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
import ru.vibeart.api.security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import ru.vibeart.api.models.entities.Role;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.models.enums.RoleEnum;
import ru.vibeart.api.repositories.RoleRepository;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.services.AuthService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailMessageProducer emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.code-expiration-time}")
    private Duration codeExpirationTime;

    @Value("${app.jwt.access-token-validity}")
    private long accessTokenValidityInMillis;

    @Value("${app.jwt.refresh-token-validity}")
    private long refreshTokenValidityInMillis;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final Pattern emailPattern = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public AuthServiceImpl(
            ModelMapper modelMapper,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailMessageProducer emailService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider
    ) {
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
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
     * Регистрация пользователя в системе
     * @param signUpRequest объект для регистрации пользователя
     */
    @Override
    public void register(SignUpRequest signUpRequest) {
        try {
            if(userRepository.existsByEmail(signUpRequest.getEmail())) {
                log.warn("Registration failed: user already exists, email={}", signUpRequest.getEmail());
                throw new ConflictException("User already exists");
            }
            if(!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
                log.warn("Registration failed: passwords do not match for email={}", signUpRequest.getEmail());
                throw new IllegalArgumentException("Passwords do not match");
            }
            if(!emailPattern.matcher(signUpRequest.getEmail()).matches()) {
                log.warn("Registration failed: Invalid email={}", signUpRequest.getEmail());
                throw new IllegalArgumentException("Invalid email");
            }

            User client = modelMapper.map(signUpRequest, User.class);
            client.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            client.setEnabled(false);
            client.setUuid(UUID.randomUUID());
            client.setCreatedAt(Instant.now());
            String code = generateSixDigitCode();
            client.setVerificationCode(code);
            client.setVerificationCodeExpiresAt(Instant.now().plus(codeExpirationTime));
            log.debug("Generated verification code for email={}: code={}, expiresAt={}",
                    signUpRequest.getEmail(), code, client.getVerificationCodeExpiresAt());

            Role role = roleRepository.findByName(RoleEnum.USER)
                    .orElseThrow(() -> {
                        log.error("Default role not found during registration");
                        return new IllegalStateException("Default role not found");
                    });
            client.setRole(role);
            userRepository.save(client);
            emailService.sendVerificationEmail(client.getEmail(), code);
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
     * Подтверждения регистрации (подтверждения почты)
     * @param verifyRequest объект для подтверждения регистрации
     */
    @Override
    public void verify(VerifyRequest verifyRequest) {
        try {
            User user = userRepository.findByEmail(verifyRequest.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Verification failed: user not found, email={}", verifyRequest.getEmail());
                        return new IllegalArgumentException("User not found");
                    });
            if(user.isEnabled()) {
                log.warn("Verification skipped: user already verified, email={}", user.getEmail());
                throw new IllegalStateException("User already verified");
            }
            if(user.getVerificationCodeExpiresAt().isBefore(Instant.now())) {
                log.warn("Verification failed: code expired for email={}", user.getEmail());
                throw new IllegalStateException("Verification code expired");
            }
            if(!user.getVerificationCode().equals(verifyRequest.getVerificationCode())) {
                log.warn("Verification failed: invalid code for email={}", user.getEmail());
                throw new IllegalArgumentException("Invalid verification code");
            }

            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
            log.info("END verify: user enabled with email={}", user.getEmail());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during verify for email={}", verifyRequest.getEmail(), ex);
            throw new ServiceException("Database error during email verification", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during verify for email={}", verifyRequest.getEmail(), ex);
            throw new ServiceException("Unexpected error during email verification", ex);
        }
    }

    @Override
    public AuthResponse login(SignInRequest signInRequest) {
        log.info("START login: email={}", signInRequest.getEmail());
        try {
            String email = signInRequest.getEmail();
            String password = signInRequest.getPassword();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
            var authentication = authenticationManager.authenticate(authToken);

            var userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("Login failed: user not found, email={}", email);
                        return new IllegalArgumentException("User not found");
                    });

            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            log.debug("User authenticated: email={}, role={}", email, role);

            String accessToken = tokenProvider.generateAccessToken(email, role);
            String refreshToken = tokenProvider.generateRefreshToken(email, role);
            log.info("END login: email={} issued tokens [accessExpiresIn={}ms, refreshExpiresIn={}ms]",
                    email, accessTokenValidityInMillis, refreshTokenValidityInMillis);

            return new AuthResponse(user.getUuid(), accessToken, refreshToken, accessTokenValidityInMillis, refreshTokenValidityInMillis);

        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email={}", signInRequest.getEmail(), ex);
            throw new IllegalArgumentException("Invalid email or password");
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during login for email={}", signInRequest.getEmail(), ex);
            throw new ServiceException("Unexpected error during login", ex);
        }
    }

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

            String email = tokenProvider.getUsernameFromJWT(providedRefreshToken);
            String role = tokenProvider.getUserRoleFromJWT(providedRefreshToken);
            log.debug("Refreshing tokens for email={}, role={}", email, role);

            String newAccessToken = tokenProvider.generateAccessToken(email, role);
            String newRefreshToken = tokenProvider.generateRefreshToken(email, role);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("Refresh failed: user not found, email={}", email);
                        return new IllegalArgumentException("User not found");
                    });
            log.info("END refresh: email={} issued new tokens", email);

            return new AuthResponse(user.getUuid(), newAccessToken, newRefreshToken, accessTokenValidityInMillis, refreshTokenValidityInMillis);

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during refresh", ex);
            throw new ServiceException("Unexpected error during token refresh", ex);
        }
    }
}
