package ru.vibeart.api.services.impl;

import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.vibeart.api.dtos.auth.*;
import ru.vibeart.api.exceptions.ConflictException;
import ru.vibeart.api.exceptions.GoneException;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.exceptions.UnauthorizedException;
import ru.vibeart.api.models.entities.VerificationCode;
import ru.vibeart.api.models.enums.VerificationCodesType;
import ru.vibeart.api.repositories.VerificationCodeRepository;
import ru.vibeart.api.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vibeart.api.models.entities.Role;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.models.enums.RoleEnum;
import ru.vibeart.api.repositories.RoleRepository;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.services.AuthService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;


/**
 * Реализация {@link AuthService}.
 * <p>
 * Использует {@link JwtTokenProvider} для генерации токенов, {@link PasswordEncoder}
 * для хеширования паролей и {@link EmailMessageProducer} для отправки кодов подтверждения
 * через RabbitMQ.
 * </p>
 */
@Service
public class AuthServiceImpl implements AuthService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailMessageProducer emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.code-expiration-time}")
    private Duration codeExpirationTime;

    @Value("${app.allow-new-code-time}")
    private Duration allowNewCodeTime;

    @Value("${app.jwt.access-token-validity}")
    private long accessTokenValidityInMillis;

    @Value("${app.jwt.refresh-token-validity}")
    private long refreshTokenValidityInMillis;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param modelMapper конвертер для преобразования DTO и сущностей
     * @param roleRepository репозиторий ролей
     * @param verificationCodeRepository репозиторий кодов подтверждения
     * @param userRepository репозиторий пользователей
     * @param passwordEncoder кодировщик паролей
     * @param emailService сервис отправки писем через RabbitMQ
     * @param authenticationManager менеджер аутентификации Spring Security
     * @param tokenProvider провайдер JWT-токенов
     */
    public AuthServiceImpl(
            ModelMapper modelMapper,
            RoleRepository roleRepository,
            VerificationCodeRepository verificationCodeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailMessageProducer emailService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider
    ) {
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    /**
     * Генерация шестизначного кода для подтверждения почты
     */
    private String generateSixDigitCode() {
        int number = secureRandom.nextInt(1_000_000);
        // Целое число, дополнить нулями слева до 6
        return String.format("%06d", number);
    }

    /**
     * <h1>Регистрация пользователя</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *    Находит неверифицированного пользователя по адресу электронной почты или создаёт его.
     *    Находит старый код подтверждения и удаляет, если он существует и создает новый.
     *    Созданный код сохраняется в базу данных и отправляется в отдельный микросервис
     *    с помощью {@link EmailMessageProducer}
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пароли не совпадают или запрос отправлен слишком рано, выбрасывается исключение
     *         {@link IllegalArgumentException} с кодом ошибки <b>400</b>
     *     </li>
     *     <li>
     *          Если пользователь с таким адресом электронной почты уже существует и верифицирован,
     *          выбрасывается {@link ConflictException} с кодом ошибки <b>409</b>
     *      </li>
     *      <li>
     *          Если роль <code>USER</code> не найдена, то выбрасывается {@link IllegalStateException}
     *          с кодом ошибки <b>500</b>
     *      </li>
     *      <li>
     *          При ошибке базы данных или любой другой ошибке, выбрасывается исключение
     *          {@link ServiceException} с кодом ответа <b>500</b>
     *      </li>
     * </ul>
     *
     * @param signUpRequest объект для регистрации пользователя
     * @throws IllegalArgumentException если пароли не совпадают или запрос отправлен слишком рано
     * @throws ConflictException если пользователь уже существует
     * @throws IllegalStateException если роль не найдена
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public void register(SignUpRequest signUpRequest, Locale locale) {
        try {
            if(!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
                log.warn("Registration failed: passwords do not match for email={}", signUpRequest.getEmail());
                throw new IllegalArgumentException("Passwords do not match");
            }

            var existingUser = userRepository.findByEmail(signUpRequest.getEmail());
            if(existingUser.isPresent() && existingUser.get().isEnabled()) {
                log.warn("Registration failed: user already exists, email={}", signUpRequest.getEmail());
                throw new ConflictException("User already exists");
            }

            String code = generateSixDigitCode();
            User client = existingUser.orElseGet(() -> {
                User newClient = modelMapper.map(signUpRequest, User.class);
                newClient.setUuid(UUID.randomUUID());
                newClient.setCreatedAt(Instant.now());

                Role role = roleRepository.findByName(RoleEnum.USER)
                        .orElseThrow(() -> {
                            log.error("Default role not found during registration");
                            return new IllegalStateException("Default role not found");
                        });
                newClient.setRole(role);
                return newClient;
            });
            client.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            client.setEnabled(false);

            userRepository.saveAndFlush(client);

            Optional<VerificationCode> oldCode = verificationCodeRepository
                    .findByUserAndType(client, VerificationCodesType.REGISTER);

            oldCode.ifPresent(i -> {
                Instant sentTime = i.getCodeExpiresAt().minus(codeExpirationTime);
                if(Instant.now().isBefore(sentTime.plus(allowNewCodeTime))) {
                    log.warn("Register rejected: verification code was requested too recently, UUID={}", client.getUuid());
                    throw new IllegalArgumentException("Please wait before requesting a new verification code");
                }

                verificationCodeRepository.delete(i);
                verificationCodeRepository.flush();
            });

            VerificationCode verificationCode = new VerificationCode(
                    code,
                    Instant.now().plus(codeExpirationTime),
                    signUpRequest.getEmail(),
                    client,
                    VerificationCodesType.REGISTER);

            log.debug("Generated register verification code for email={}: code={}, expiresAt={}",
                    signUpRequest.getEmail(), code, verificationCode.getCodeExpiresAt());

            verificationCodeRepository.save(verificationCode);
            emailService.sendRegisterVerificationEmail(client.getEmail(), code, locale.getLanguage());
            log.info("END register: user saved and verification email sent to={}", client.getEmail());
        } catch (IllegalArgumentException | IllegalStateException | ConflictException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during register for email={}", signUpRequest.getEmail(), ex);
            throw new ServiceException("Database error during registration", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during register for email={}", signUpRequest.getEmail(), ex);
            throw new ServiceException("Unexpected error during registration", ex);
        }
    }

    /**
     * <h1>Повторная отправка кода подтверждения почты</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *    Находит пользователя по адресу электронной почты, удаляет его старый код подтверждения
     *    и генерирует новый. Созданный код отправляется в отдельный микросервис
     *    с помощью {@link EmailMessageProducer}
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не найден, выбрасывается исключение
     *         {@link ResourceNotFoundException} с кодом ошибки <b>404</b>
     *     </li>
     *     <li>
     *         Если пользователь уже верифицирован, выбрасывается исключение
     *         {@link ConflictException} с кодом ошибки <b>409</b>
     *     </li>
     *     <li>
     *         Если запрос отправлен раньше двух минут с момента отправки предыдущего кода,
     *         выбрасывается исключение {@link IllegalArgumentException} с кодом ошибки <b>400</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается исключение
     *         {@link ServiceException} с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param sendCodeRequest объект с адресом почты для повторной отправки кода
     * @param locale объект locale с текущим языком пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     * @throws ConflictException если пользователь уже верифицирован
     * @throws IllegalArgumentException если запрос отправлен слишком рано
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public void send(SendCodeRequest sendCodeRequest, Locale locale) {
        try {
            String email = sendCodeRequest.getEmail();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Sending code again failed: user not found, email={}", email);
                        return new ResourceNotFoundException("User not found");
                    });
            if(user.isEnabled()) {
                log.warn("Sending code again skipped: user already verified, email={}", user.getEmail());
                throw new ConflictException("User already verified");
            }

            Optional<VerificationCode> oldCode = verificationCodeRepository
                    .findByUserAndType(user, VerificationCodesType.REGISTER);

            oldCode.ifPresent(i -> {
                Instant sentTime = i.getCodeExpiresAt().minus(codeExpirationTime);
                if(Instant.now().isBefore(sentTime.plus(allowNewCodeTime))) {
                    log.warn("Sending code again rejected: too soon, email={}", email);
                    throw new IllegalArgumentException("Please wait before requesting a new verification code");
                }
                verificationCodeRepository.delete(i);
                verificationCodeRepository.flush();
            });

            String code = generateSixDigitCode();
            VerificationCode verificationCode = new VerificationCode(
                    code,
                    Instant.now().plus(codeExpirationTime),
                    email,
                    user,
                    VerificationCodesType.REGISTER);
            log.debug("Generated register verification code again for email={}: code={}, expiresAt={}",
                    email, code, verificationCode.getCodeExpiresAt());

            verificationCodeRepository.save(verificationCode);
            emailService.sendRegisterVerificationEmail(user.getEmail(), code, locale.getLanguage());
            log.info("END send: verification code resent to email={}", user.getEmail());
        } catch (ResourceNotFoundException | ConflictException | IllegalArgumentException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during sending code again for email={}", sendCodeRequest.getEmail(), ex);
            throw new ServiceException("Database error during sending code", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during sending code again for email={}", sendCodeRequest.getEmail(), ex);
            throw new ServiceException("Unexpected error during sending code", ex);
        }
    }

    /**
     * <h1>Подтверждения регистрации и авторизация</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *    Находит пользователя в базе данных по адресу электронной почты
     *    и проверяет присланный код подтверждения с кодом, сохранённым в базе данных и
     *    авторизует пользователя, возвращая объект авторизации.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь или код подтверждения не найдены в базе данных,
     *         бросается исключение {@link ResourceNotFoundException} с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         Если пользователь уже верифицирован, бросается исключение {@link ConflictException}
     *         с кодом ответа <b>409</b>
     *     </li>
     *     <li>
     *         Если код подтверждения неверный, выбрасывается исключение {@link IllegalArgumentException}
     *         с кодом ответа <b>400</b>
     *     </li>
     *     <li>
     *         Если код подтверждения истёк, выбрасывается исключение {@link GoneException}
     *         с кодом ответа <b>410</b>
     *     </li>
     *     <li>
     *          При ошибке базы данных или любой другой ошибке, выбрасывается исключение {@link ServiceException}
     *          с кодом ответа <b>500</b>
     *      </li>
     * </ul>
     *
     * @param verifyRequest объект для подтверждения регистрации, включающий электронную почту и код подтверждения
     * @return Объект с UUID пользователя, токенами авторизации (access и refresh) и временем их действия
     * @throws ResourceNotFoundException если пользователь или код подтверждения не найдены
     * @throws ConflictException если пользователь уже верифицирован
     * @throws IllegalArgumentException если код подтверждения неверный
     * @throws GoneException если код подтверждения истёк
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public AuthResponse verify(VerifyRequest verifyRequest) {
        try {
            String email = verifyRequest.getEmail();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Verification failed: user not found, email={}", email);
                        return new ResourceNotFoundException("User not found");
                    });
            if(user.isEnabled()) {
                log.warn("Verification skipped: user already verified, email={}", user.getEmail());
                throw new ConflictException("User already verified");
            }

            VerificationCode verificationCode = verificationCodeRepository
                    .findByUserAndType(user, VerificationCodesType.REGISTER)
                    .orElseThrow(() -> {
                        log.warn("Verification failed: code not found for email={}", email);
                        return new ResourceNotFoundException("Verification code not found");
                    });

            if(!verificationCode.getCode().equals(verifyRequest.getVerificationCode())) {
                log.warn("Verification failed: invalid code for email={}", user.getEmail());
                throw new IllegalArgumentException("Invalid verification code");
            }
            if(verificationCode.getCodeExpiresAt().isBefore(Instant.now())) {
                log.warn("Verification failed: code expired for email={}", user.getEmail());
                throw new GoneException("Verification code expired");
            }

            user.setEnabled(true);
            verificationCodeRepository.delete(verificationCode);

            userRepository.save(user);
            log.info("User verified: user enabled with email={}", user.getEmail());

            String role = user.getRole().getName().name();
            log.debug("User authenticated after verification: email={}, role={}", email, role);

            String accessToken = tokenProvider.generateAccessToken(user.getUuid().toString(), role);
            String refreshToken = tokenProvider.generateRefreshToken(user.getUuid().toString(), role);
            log.info("END verify: email={} issued tokens [accessExpiresIn={}ms, refreshExpiresIn={}ms]",
                    email, accessTokenValidityInMillis, refreshTokenValidityInMillis);

            return new AuthResponse(user.getUuid(), accessToken, refreshToken, accessTokenValidityInMillis, refreshTokenValidityInMillis);
        } catch (IllegalArgumentException
                 | IllegalStateException
                 | ConflictException
                 | ResourceNotFoundException
                 | GoneException ex
        ) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during verify for email={}", verifyRequest.getEmail(), ex);
            throw new ServiceException("Database error during email verification", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during verify for email={}", verifyRequest.getEmail(), ex);
            throw new ServiceException("Unexpected error during email verification", ex);
        }
    }

    /**
     * <h1>Авторизация пользователя по адресу электронной почты и паролю</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Ищет пользователя и проверяет авторизационные данные. После проверки генерирует
     *     JWT токены и возвращает объект авторизации
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если ввёденные данные неверные или пользователь не найден,
     *         выбрасывается {@link UnauthorizedException} с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *      </li>
     * </ul>
     *
     * @param signInRequest объект с учётными данными для входа
     * @return Объект с UUID пользователя, токенами авторизации (access и refresh) и временем их действия
     * @throws UnauthorizedException если ввёденные данные неверные или пользователь не найден
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    public AuthResponse login(SignInRequest signInRequest) {
        log.info("START login: email={}", signInRequest.getEmail());
        try {
            String email = signInRequest.getEmail();
            String password = signInRequest.getPassword();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("Login failed: user not found, email={}", email);
                        return new UnauthorizedException("Invalid email or password");
                    });

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user.getUuid(),
                    password);
            var authentication = authenticationManager.authenticate(authToken);

            var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            log.debug("User authenticated: email={}, role={}", email, role);

            String accessToken = tokenProvider.generateAccessToken(user.getUuid().toString(), role);
            String refreshToken = tokenProvider.generateRefreshToken(user.getUuid().toString(), role);
            log.info("END login: email={} issued tokens [accessExpiresIn={}ms, refreshExpiresIn={}ms]",
                    email, accessTokenValidityInMillis, refreshTokenValidityInMillis);

            return new AuthResponse(user.getUuid(), accessToken, refreshToken, accessTokenValidityInMillis, refreshTokenValidityInMillis);

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email={}", signInRequest.getEmail(), ex);
            throw new UnauthorizedException("Invalid email or password");
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during login for email={}", signInRequest.getEmail(), ex);
            throw new ServiceException("Database error during email verification", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during login for email={}", signInRequest.getEmail(), ex);
            throw new ServiceException("Unexpected error during login", ex);
        }
    }

    /**
     * <h1>Обновление access и refresh токенов по действующему refresh токену</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Проверяет существование пользователя и генерирует новые access и refresh токены
     *     по переданному refresh токену. После генерации токенов
     *     возвращает объект авторизации
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если переданный refresh токен неверный или тип токена не refresh,
     *         выбрасывается {@link IllegalArgumentException} с кодом ответа <b>400</b>
     *     </li>
     *     <li>
     *         Если пользователь не найден, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *      </li>
     * </ul>
     *
     * @param refreshRequest объект с refresh токеном
     * @return Объект с UUID пользователя, токенами авторизации (access и refresh) и временем их действия
     * @throws IllegalArgumentException если токен неверный или тип токена не совпадает
     * @throws ResourceNotFoundException если пользователь не найден
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    public AuthResponse refresh(RefreshRequest refreshRequest) {
        log.info("START refresh token");
        try {
            String providedRefreshToken = refreshRequest.getRefreshToken();

            if (!tokenProvider.validateToken(providedRefreshToken)) {
                log.warn("Refresh failed: invalid token");
                throw new IllegalArgumentException("Invalid refresh token");
            }
            if (!"refresh".equals(tokenProvider.getTokenType(providedRefreshToken))) {
                log.warn("Refresh failed: token is not a refresh token");
                throw new IllegalArgumentException("Provided token is not a refresh token");
            }

            String uuid = tokenProvider.getUsernameFromJWT(providedRefreshToken);
            String role = tokenProvider.getUserRoleFromJWT(providedRefreshToken);
            log.debug("Refreshing tokens for uuid={}, role={}", uuid, role);

            User user = userRepository.findByUuid(UUID.fromString(uuid))
                    .orElseThrow(() -> {
                        log.error("Refresh failed: user not found, uuid={}", uuid);
                        return new ResourceNotFoundException("User not found");
                    });

            String newAccessToken = tokenProvider.generateAccessToken(uuid, role);
            String newRefreshToken = tokenProvider.generateRefreshToken(uuid, role);
            log.info("END refresh: uuid={} issued new tokens", uuid);

            return new AuthResponse(user.getUuid(), newAccessToken, newRefreshToken, accessTokenValidityInMillis, refreshTokenValidityInMillis);

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during refresh", ex);
            throw new ServiceException("Database error during email verification", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during refresh", ex);
            throw new ServiceException("Unexpected error during token refresh", ex);
        }
    }
}
