package ru.vibeart.api.services.impl;

import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.vibeart.api.dtos.chat.ChatResponse;
import ru.vibeart.api.dtos.chat.CreateChatRequest;
import ru.vibeart.api.dtos.common.StompResponse;
import ru.vibeart.api.dtos.message.MessageResponse;
import ru.vibeart.api.dtos.user.UserResponse;
import ru.vibeart.api.events.DeleteEmptyChatEvent;
import ru.vibeart.api.exceptions.ConflictException;
import ru.vibeart.api.exceptions.ForbiddenException;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.exceptions.UnauthorizedException;
import ru.vibeart.api.models.entities.Chat;
import ru.vibeart.api.models.entities.ChatMember;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.models.entities.Subscription;
import ru.vibeart.api.models.enums.StompEvent;
import ru.vibeart.api.repositories.ChatMemberRepository;
import ru.vibeart.api.repositories.ChatRepository;
import ru.vibeart.api.repositories.MessageRepository;
import ru.vibeart.api.repositories.SubscriptionRepository;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.services.ChatService;
import ru.vibeart.api.utils.AuthUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация {@link ChatService}.
 * <p>
 * Использует {@link ModelMapper} для преобразования сущности {@link Chat} в DTO,
 * {@link AuthUtil} для определения текущего аутентифицированного пользователя
 * и {@link ApplicationEventPublisher} для публикации событий.
 * </p>
 */
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    private static final String stompTargetChat = "chat";

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param chatRepository репозиторий чатов
     * @param chatMemberRepository репозиторий участников чатов
     * @param messageRepository репозиторий сообщений
     * @param userRepository репозиторий пользователей
     * @param subscriptionRepository репозиторий подписок пользователей
     * @param authUtil утилита для получения данных текущего аутентифицированного пользователя
     * @param modelMapper конвертер для преобразования DTO и сущностей
     * @param eventPublisher издатель событий приложения, используется для публикации {@link DeleteEmptyChatEvent}
     */
    public ChatServiceImpl(
            ChatRepository chatRepository,
            ChatMemberRepository chatMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            SubscriptionRepository subscriptionRepository,
            AuthUtil authUtil,
            ModelMapper modelMapper,
            ApplicationEventPublisher eventPublisher,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.authUtil = authUtil;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * <h1>Получение списка чатов текущего пользователя</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Возвращает постраничный список активных чатов, в которых состоит текущий
     *     пользователь, вместе с данными участников, статусом подписки на них
     *     и последним сообщением каждого чата.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param pageable параметры пагинации
     * @return страница с данными чатов
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ChatResponse> getChats(Pageable pageable) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Getting chats error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });
            Page<Chat> chats = chatRepository.findByMembers_User_UuidAndActiveTrue(clientId, pageable);

            return chats.map(chat -> {
                ChatResponse response = modelMapper.map(chat, ChatResponse.class);
                response.setMembers(chat.getMembers().stream()
                        .map(member -> {
                            User memberUser = member.getUser();
                            UserResponse memberResponse = modelMapper.map(memberUser, UserResponse.class);
                            memberResponse.setSubscribed(
                                    memberUser.getUuid().equals(clientId) ? null :
                                            subscriptionRepository.findByFollowerAndFollowing(client, memberUser)
                                                    .map(Subscription::isActive).orElse(false)
                            );
                            return memberResponse;
                        })
                        .toList());
                messageRepository.findFirstByChat_UuidOrderByCreatedAtDesc(chat.getUuid())
                        .ifPresent(lastMessage -> {
                            MessageResponse lastMessageResponse = modelMapper.map(lastMessage, MessageResponse.class);
                            lastMessageResponse.setAuthor(modelMapper.map(lastMessage.getAuthor(), UserResponse.class));
                            response.setLastMessage(lastMessageResponse);
                        });
                return response;
            });
        } catch (DataAccessException ex) {
            log.error("Database error during getting chats, client UUID={}", clientId, ex);
            throw new ServiceException("Database error during getting chats", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during getting chats, client UUID={}", clientId, ex);
            throw new ServiceException("Unexpected error getting chats", ex);
        }
    }

    /**
     * <h1>Получение данных собеседника личного чата</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Возвращает данные единственного собеседника личного чата — участника,
     *     отличного от текущего пользователя. Текущий пользователь должен состоять
     *     в чате, а чат должен быть личным.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если чат не является личным, выбрасывается {@link IllegalArgumentException}
     *         с кодом ответа <b>400</b>
     *     </li>
     *     <li>
     *         Если пользователь не состоит в этом чате, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если чат не найден или в нём не осталось собеседника, выбрасывается
     *         {@link ResourceNotFoundException} с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param id UUID чата
     * @return объект с данными собеседника
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws IllegalArgumentException если чат не является личным
     * @throws ForbiddenException если пользователь не состоит в этом чате
     * @throws ResourceNotFoundException если чат не найден или в нём не осталось собеседника
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse getChatCompanion(UUID id) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Getting chat companion error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });

            Chat chat = chatRepository.findByUuidAndActiveTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

            chatMemberRepository.findByChat_UuidAndUser_Uuid(chat.getUuid(), clientId)
                    .orElseThrow(() -> {
                        log.warn("Getting chat companion warn: user is not a member of chat, user UUID={}, chat UUID={}", clientId, id);
                        return new ForbiddenException("You are not a member of this chat");
                    });

            if(!chat.isDirect()) {
                log.warn("Getting chat companion warn: chat is not direct, chat UUID={}", id);
                throw new IllegalArgumentException("Chat is not direct");
            }

            ChatMember companion = chatMemberRepository.findByChat_UuidAndUser_UuidNot(chat.getUuid(), clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Companion not found"));

            User companionUser = companion.getUser();
            UserResponse response = modelMapper.map(companionUser, UserResponse.class);
            response.setSubscribed(
                    subscriptionRepository.findByFollowerAndFollowing(client, companionUser)
                            .map(Subscription::isActive).orElse(false)
            );
            return response;
        } catch (ResourceNotFoundException | ServiceException | ForbiddenException | IllegalArgumentException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during getting chat companion, chat UUID={}", id, ex);
            throw new ServiceException("Database error during getting chat companion", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during getting chat companion, chat UUID={}", id, ex);
            throw new ServiceException("Unexpected error getting chat companion", ex);
        }
    }

    /**
     * <h1>Получение личного чата с собеседником</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Возвращает личный чат текущего пользователя с указанным собеседником,
     *     если он уже существует.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если запрошен чат с самим собой, выбрасывается {@link IllegalArgumentException}
     *         с кодом ответа <b>400</b>
     *     </li>
     *     <li>
     *         Если собеседник не найден, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         Если личного чата с собеседником ещё нет, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param companionUuid UUID собеседника
     * @return объект с данными чата
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws IllegalArgumentException если запрошен чат с самим собой
     * @throws ResourceNotFoundException если собеседник не найден или чата с ним ещё нет
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatByCompanion(UUID companionUuid) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Getting chat by companion error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });

            if(clientId.equals(companionUuid)) {
                log.warn("Getting chat by companion warn: companion is client itself, client UUID={}", clientId);
                throw new IllegalArgumentException("Companion cannot be yourself");
            }

            userRepository.findByUuid(companionUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Companion not found"));

            Chat chat = chatRepository.findChatBetweenUsers(clientId, companionUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat with this companion not found"));

            ChatResponse response = modelMapper.map(chat, ChatResponse.class);
            response.setMembers(chat.getMembers().stream()
                    .map(member -> {
                        User memberUser = member.getUser();
                        UserResponse memberResponse = modelMapper.map(memberUser, UserResponse.class);
                        memberResponse.setSubscribed(
                                memberUser.getUuid().equals(clientId) ? null :
                                        subscriptionRepository.findByFollowerAndFollowing(client, memberUser)
                                                .map(Subscription::isActive).orElse(false)
                        );
                        return memberResponse;
                    })
                    .toList());
            messageRepository.findFirstByChat_UuidOrderByCreatedAtDesc(chat.getUuid())
                    .ifPresent(lastMessage -> {
                        MessageResponse lastMessageResponse = modelMapper.map(lastMessage, MessageResponse.class);
                        lastMessageResponse.setAuthor(modelMapper.map(lastMessage.getAuthor(), UserResponse.class));
                        response.setLastMessage(lastMessageResponse);
                    });
            return response;
        } catch (ResourceNotFoundException | ServiceException | IllegalArgumentException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during getting chat by companion, companion UUID={}", companionUuid, ex);
            throw new ServiceException("Database error during getting chat by companion", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during getting chat by companion, companion UUID={}", companionUuid, ex);
            throw new ServiceException("Unexpected error getting chat by companion", ex);
        }
    }

    /**
     * <h1>Создание чата</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Создаёт чат с произвольным числом участников от имени текущего пользователя.
     *     Текущий пользователь автоматически добавляется как владелец чата. Если указан
     *     ровно один участник и личного чата с ним ещё нет, созданный чат помечается личным.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если список участников содержит самого пользователя, выбрасывается
     *         {@link IllegalArgumentException} с кодом ответа <b>400</b>
     *     </li>
     *     <li>
     *         Если профиль текущего пользователя отключён, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если один или несколько участников не найдены, выбрасывается
     *         {@link ResourceNotFoundException} с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         Если профиль одного или нескольких участников отключён, выбрасывается
     *         {@link ConflictException} с кодом ответа <b>409</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param createChatRequest объект с UUID участников чата
     * @return объект с данными созданного чата
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws IllegalArgumentException если список участников содержит самого пользователя
     * @throws ForbiddenException если профиль текущего пользователя отключён
     * @throws ResourceNotFoundException если один или несколько участников не найдены
     * @throws ConflictException если профиль одного или нескольких участников отключён
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public ChatResponse createChat(CreateChatRequest createChatRequest) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Creating chat error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });
            if(!client.isEnabled()) {
                log.warn("Creating chat warn: client is not enabled, client UUID={}", clientId);
                throw new ForbiddenException("Your profile is disabled");
            }

            List<UUID> memberUuids = createChatRequest.getMemberUuids().stream().distinct().toList();
            if(memberUuids.contains(clientId)) {
                log.warn("Creating chat warn: members list contains client itself, client UUID={}", clientId);
                throw new IllegalArgumentException("Members list cannot contain yourself");
            }

            List<User> members = userRepository.findByUuidIn(memberUuids);
            if(members.size() != memberUuids.size()) {
                log.warn("Creating chat warn: some members not found, requested UUIDs={}", memberUuids);
                throw new ResourceNotFoundException("Some members not found");
            }
            for(User member : members) {
                if(!member.isEnabled()) {
                    log.warn("Creating chat warn: member is not enabled, member UUID={}", member.getUuid());
                    throw new ConflictException("Some members have an inactive profile");
                }
            }

            boolean isDirect = memberUuids.size() == 1
                    && chatRepository.findChatBetweenUsers(clientId, memberUuids.get(0)).isEmpty();

            Instant currentTime = Instant.now();
            Chat chat = chatRepository.save(new Chat(UUID.randomUUID(), currentTime, isDirect));

            chatMemberRepository.save(new ChatMember(chat, client, true, true, currentTime));
            for(User member : members) {
                chatMemberRepository.save(new ChatMember(chat, member, true, false, currentTime));
                StompResponse<UUID> chatCreatedEvent = new StompResponse<>(stompTargetChat, StompEvent.CREATED, chat.getUuid(), chat.getUuid());
                messagingTemplate.convertAndSendToUser(member.getEmail(), "/topic/chats", chatCreatedEvent);
            }

            List<UserResponse> memberResponses = new ArrayList<>();
            UserResponse clientResponse = modelMapper.map(client, UserResponse.class);
            clientResponse.setSubscribed(null);
            memberResponses.add(clientResponse);
            for(User member : members) {
                UserResponse memberResponse = modelMapper.map(member, UserResponse.class);
                memberResponse.setSubscribed(
                        subscriptionRepository.findByFollowerAndFollowing(client, member)
                                .map(Subscription::isActive).orElse(false)
                );
                memberResponses.add(memberResponse);
            }

            ChatResponse response = modelMapper.map(chat, ChatResponse.class);
            response.setMembers(memberResponses);
            return response;
        } catch (ResourceNotFoundException | ServiceException | IllegalArgumentException | ForbiddenException | ConflictException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during creating chat, client UUID={}", clientId, ex);
            throw new ServiceException("Database error during creating chat", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during creating chat, client UUID={}", clientId, ex);
            throw new ServiceException("Unexpected error creating chat", ex);
        }
    }

    /**
     * <h1>Выход из чата</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Удаляет текущего пользователя из участников чата. Если выходит владелец чата
     *     и другие участники остаются, владение передаётся участнику, вступившему раньше
     *     остальных. Если после выхода в чате не остаётся участников, публикуется
     *     {@link DeleteEmptyChatEvent} для асинхронной деактивации чата.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если пользователь не состоит в этом чате, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если чат не найден, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param id UUID чата
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws ForbiddenException если пользователь не состоит в этом чате
     * @throws ResourceNotFoundException если чат не найден
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public void leaveChat(UUID id) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Leaving chat error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });

            Chat chat = chatRepository.findByUuidAndActiveTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

            ChatMember member = chatMemberRepository.findByChat_UuidAndUser_Uuid(chat.getUuid(), clientId)
                    .orElseThrow(() -> {
                        log.warn("Leaving chat warn: user is not a member of chat, user UUID={}, chat UUID={}", clientId, id);
                        return new ForbiddenException("You are not a member of this chat");
                    });

            chatMemberRepository.delete(member);

            if(chatMemberRepository.countByChat_Uuid(chat.getUuid()) == 0) {
                eventPublisher.publishEvent(new DeleteEmptyChatEvent(chat.getUuid()));
            } else if(member.getOwner()) {
                ChatMember newOwner = chatMemberRepository.findFirstByChat_UuidOrderByJoinedAtAsc(chat.getUuid())
                        .orElseThrow(() -> {
                            log.error("Leaving chat error: chat member count inconsistent during ownership transfer, chat UUID={}", id);
                            return new ServiceException("Unexpected error transferring chat ownership");
                        });
                newOwner.setOwner(true);
                chatMemberRepository.save(newOwner);
            }
        } catch (ResourceNotFoundException | ServiceException | ForbiddenException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during leaving chat, chat UUID={}", id, ex);
            throw new ServiceException("Database error during leaving chat", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during leaving chat, chat UUID={}", id, ex);
            throw new ServiceException("Unexpected error leaving chat", ex);
        }
    }

    /**
     * <h1>Деактивация опустевшего чата</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Деактивирует чат, если в нём не осталось участников. Выполняется в отдельной
     *     транзакции ({@code Propagation.REQUIRES_NEW}), так как срабатывает после того,
     *     как транзакция, приведшая к вызову, уже завершена.
     * </p>
     *
     * @param id UUID чата
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateIfEmpty(UUID id) {
        try {
            chatRepository.findByUuidAndActiveTrue(id)
                    .ifPresent(chat -> {
                        if(chatMemberRepository.countByChat_Uuid(id) == 0) {
                            chat.setActive(false);
                            chatRepository.save(chat);
                            log.info("Chat deactivated as empty, chat UUID={}", id);
                        }
                    });
        } catch (DataAccessException ex) {
            log.error("Database error during deactivating empty chat, chat UUID={}", id, ex);
            throw new ServiceException("Database error during deactivating empty chat", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during deactivating empty chat, chat UUID={}", id, ex);
            throw new ServiceException("Unexpected error deactivating empty chat", ex);
        }
    }
}
