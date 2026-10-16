package ru.vibeart.api.services.impl;

import org.hibernate.service.spi.ServiceException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vibeart.api.dtos.common.StompResponse;
import ru.vibeart.api.dtos.message.CreateMessageRequest;
import ru.vibeart.api.dtos.message.MessageResponse;
import ru.vibeart.api.dtos.message.UpdateMessageRequest;
import ru.vibeart.api.dtos.user.UserResponse;
import ru.vibeart.api.exceptions.ConflictException;
import ru.vibeart.api.exceptions.ForbiddenException;
import ru.vibeart.api.exceptions.ResourceNotFoundException;
import ru.vibeart.api.exceptions.UnauthorizedException;
import ru.vibeart.api.models.entities.Chat;
import ru.vibeart.api.models.entities.ChatMember;
import ru.vibeart.api.models.entities.Message;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.models.enums.StompEvent;
import ru.vibeart.api.repositories.ChatMemberRepository;
import ru.vibeart.api.repositories.ChatRepository;
import ru.vibeart.api.repositories.MessageRepository;
import ru.vibeart.api.repositories.UserRepository;
import ru.vibeart.api.services.MessageService;
import ru.vibeart.api.utils.AuthUtil;

import java.time.Instant;
import java.util.UUID;

/**
 * Реализация {@link MessageService}.
 * <p>
 * Использует {@link ModelMapper} для преобразования сущности {@link Message} в DTO
 * и {@link AuthUtil} для определения текущего аутентифицированного пользователя.
 * </p>
 */
@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private static final String stompTargetMessage = "message";
    private static final String stompTargetChat = "chat";

    /**
     * Конструктор с внедрением зависимостей.
     *
     * @param messageRepository репозиторий сообщений
     * @param chatRepository репозиторий чатов
     * @param authUtil утилита для получения данных текущего аутентифицированного пользователя
     * @param userRepository репозиторий пользователей
     * @param chatMemberRepository репозиторий участников чатов
     * @param modelMapper конвертер для преобразования DTO и сущностей
     */
    public MessageServiceImpl(
            MessageRepository messageRepository,
            ChatRepository chatRepository,
            AuthUtil authUtil,
            UserRepository userRepository,
            ChatMemberRepository chatMemberRepository,
            ModelMapper modelMapper,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messageRepository = messageRepository;
        this.authUtil = authUtil;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.modelMapper = modelMapper;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * <h1>Получение сообщений чата</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Возвращает сообщения чата с курсорной пагинацией, от новых к старым.
     *     Доступно только участникам чата.
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
     * @param before дата, раньше которой нужно вернуть сообщения
     * @param limit максимальное количество сообщений за один запрос
     * @return срез с данными сообщений
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws ForbiddenException если пользователь не состоит в этом чате
     * @throws ResourceNotFoundException если чат не найден
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional(readOnly = true)
    public Slice<MessageResponse> getMessages(UUID id, Instant before, int limit) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Getting messages error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });
            Chat chat = chatRepository.findByUuidAndActiveTrue(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));
            chatMemberRepository.findByChat_UuidAndUser_Uuid(chat.getUuid(), clientId)
                    .orElseThrow(() -> {
                        log.warn("Getting messages error: user is not belongs to chat, user UUID={}, chat UUID={}", clientId, id);
                        return new ForbiddenException("You have not access to this messages");
                    });

            Instant cursor = before != null ? before : Instant.now();
            Limit resultLimit = Limit.of(Math.clamp(limit, 1, 100));

            Slice<Message> messages = messageRepository.findByChat_UuidAndCreatedAtLessThanOrderByCreatedAtDesc(chat.getUuid(), cursor, resultLimit);
            return messages.map(message -> {
                MessageResponse response = modelMapper.map(message, MessageResponse.class);
                response.setAuthor(modelMapper.map(message.getAuthor(), UserResponse.class));
                return response;
            });
        } catch (ResourceNotFoundException | ServiceException | ForbiddenException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during getting messages between users with chat UUID={}", id, ex);
            throw new ServiceException("Database error during getting messages", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during getting messages between users with chat UUID={}", id, ex);
            throw new ServiceException("Unexpected error getting messages", ex);
        }
    }

    /**
     * <h1>Создание сообщения</h1>
     *
     * <h2>Назначение</h2>
     * <p>
     *     Создаёт сообщение собеседнику от имени текущего пользователя. Если личного
     *     чата с собеседником ещё нет, он создаётся автоматически.
     * </p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если сообщение отправляется самому себе, выбрасывается
     *         {@link IllegalArgumentException} с кодом ответа <b>400</b>
     *     </li>
     *     <li>
     *         Если профиль текущего пользователя отключён, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если собеседник не найден, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         Если профиль собеседника отключён, выбрасывается {@link ConflictException}
     *         с кодом ответа <b>409</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param createMessageRequest объект с данными нового сообщения
     * @return объект с данными созданного сообщения
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws IllegalArgumentException если сообщение отправляется самому себе
     * @throws ForbiddenException если профиль текущего пользователя отключён
     * @throws ResourceNotFoundException если собеседник не найден
     * @throws ConflictException если профиль собеседника отключён
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public MessageResponse createMessage(CreateMessageRequest createMessageRequest) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Creating message error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });
            User companion = userRepository.findByUuid(createMessageRequest.getCompanionUuid())
                    .orElseThrow(() -> new ResourceNotFoundException("Provided user not found"));
            if(!client.isEnabled()) {
                log.warn("Creating message warn: client is not enabled, client UUID={}", clientId);
                throw new ForbiddenException("Your profile is disabled");
            }
            if(clientId.equals(createMessageRequest.getCompanionUuid())) {
                log.warn("Creating message warn: messages to oneself are denied, UUID={}", clientId);
                throw new IllegalArgumentException("You cannot send a message to yourself");
            }
            if(!companion.isEnabled()) {
                log.warn("Creating message warn: companion is not enabled, companion UUID={}", createMessageRequest.getCompanionUuid());
                throw new ConflictException("Provided user has an inactive profile");
            }

            Instant currentTime = Instant.now();
            Chat chat = chatRepository.findChatBetweenUsers(clientId, createMessageRequest.getCompanionUuid())
                    .orElseGet(() -> {
                        Chat newChat = chatRepository.save(new Chat(UUID.randomUUID(), currentTime, true));
                        chatMemberRepository.save(new ChatMember(newChat, client, true, true, currentTime));
                        chatMemberRepository.save(new ChatMember(newChat, companion, true, false, currentTime));

                        StompResponse<UUID> chatCreatedEvent = new StompResponse<>(stompTargetChat, StompEvent.CREATED, newChat.getUuid(), newChat.getUuid());
                        messagingTemplate.convertAndSendToUser(companion.getEmail(), "/topic/chats", chatCreatedEvent);
                        return newChat;
                    });

            Message message = modelMapper.map(createMessageRequest, Message.class);
            message.setUuid(UUID.randomUUID());
            message.setAuthor(client);
            message.setChat(chat);
            message.setCreatedAt(currentTime);
            messageRepository.save(message);
            MessageResponse response = modelMapper.map(message, MessageResponse.class);
            response.setAuthor(modelMapper.map(client, UserResponse.class));

            StompResponse<MessageResponse> stompResponse = new StompResponse<>(stompTargetMessage, StompEvent.CREATED, chat.getUuid(), response);
            sendToChatMembers(chat.getUuid(), stompResponse);
            return response;
        } catch (ResourceNotFoundException | ServiceException | IllegalArgumentException | ForbiddenException | ConflictException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during creating message between users {}, {}", clientId, createMessageRequest.getCompanionUuid(), ex);
            throw new ServiceException("Database error during getting messages", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during creating message between users {}, {}", clientId, createMessageRequest.getCompanionUuid(), ex);
            throw new ServiceException("Unexpected error getting messages", ex);
        }
    }

    /**
     * <h1>Изменение сообщения по UUID</h1>
     *
     * <h2>Назначение</h2>
     * <p>Находит сообщение по UUID, изменяет его текст и возвращает.</p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если профиль текущего пользователя отключён, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если запрос отправлен не автором сообщения, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если сообщение не найдено, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param id UUID сообщения
     * @param updateMessageRequest объект с новыми данными сообщения
     * @return объект с данными изменённого сообщения
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws ForbiddenException если профиль текущего пользователя отключён или запрос отправлен не автором сообщения
     * @throws ResourceNotFoundException если сообщение не найдено
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public MessageResponse updateMessage(UUID id, UpdateMessageRequest updateMessageRequest) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Updating message error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });
            if(!client.isEnabled()) {
                log.warn("Updating message warn: client is not enabled, client UUID={}", clientId);
                throw new ForbiddenException("Your profile is disabled");
            }

            Message message = messageRepository.findByUuid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
            if(!message.getAuthor().getUuid().equals(clientId)) {
                log.warn("Update message warn: client is not author, message UUID={}, client UUID={}", id, clientId);
                throw new ForbiddenException("You cannot edit this message");
            }
            chatMemberRepository.findByChat_UuidAndUser_Uuid(message.getChat().getUuid(), clientId)
                    .orElseThrow(() -> {
                        log.warn("Update message warn: client is not a member of chat, message UUID={}, client UUID={}", id, clientId);
                        return new ForbiddenException("You are not a member of this chat");
                    });

            message.setText(updateMessageRequest.getText());
            messageRepository.save(message);
            MessageResponse response = modelMapper.map(message, MessageResponse.class);
            response.setAuthor(modelMapper.map(message.getAuthor(), UserResponse.class));

            StompResponse<MessageResponse> stompResponse = new StompResponse<>(stompTargetMessage, StompEvent.UPDATED, message.getChat().getUuid(), response);
            sendToChatMembers(message.getChat().getUuid(), stompResponse);
            return response;
        } catch (ResourceNotFoundException | ServiceException | ForbiddenException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during updating message, message UUID={}", id, ex);
            throw new ServiceException("Database error during updating message", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during updating message, message UUID={}", id, ex);
            throw new ServiceException("Unexpected error updating message", ex);
        }
    }

    /**
     * <h1>Удаление сообщения по UUID</h1>
     *
     * <h2>Назначение</h2>
     * <p>Находит сообщение по UUID и удаляет его.</p>
     *
     * <h3>Исключения:</h3>
     * <ul>
     *     <li>
     *         Если пользователь не авторизован, выбрасывается {@link UnauthorizedException}
     *         с кодом ответа <b>401</b>
     *     </li>
     *     <li>
     *         Если профиль текущего пользователя отключён, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если запрос отправлен не автором сообщения, выбрасывается {@link ForbiddenException}
     *         с кодом ответа <b>403</b>
     *     </li>
     *     <li>
     *         Если сообщение не найдено, выбрасывается {@link ResourceNotFoundException}
     *         с кодом ответа <b>404</b>
     *     </li>
     *     <li>
     *         При ошибке базы данных или любой другой ошибке, выбрасывается {@link ServiceException}
     *         с кодом ответа <b>500</b>
     *     </li>
     * </ul>
     *
     * @param id UUID сообщения
     * @throws UnauthorizedException если пользователь не авторизован
     * @throws ForbiddenException если профиль текущего пользователя отключён или запрос отправлен не автором сообщения
     * @throws ResourceNotFoundException если сообщение не найдено
     * @throws ServiceException если произошла ошибка базы данных или сервера
     */
    @Override
    @Transactional
    public void deleteMessage(UUID id) {
        UUID clientId = authUtil.getPrincipalUuid();

        try {
            User client = userRepository.findByUuid(clientId)
                    .orElseThrow(() -> {
                        log.error("Deleting message error: principal user not found with UUID={}", clientId);
                        return new ServiceException("Principal user not found");
                    });
            if(!client.isEnabled()) {
                log.warn("Deleting message warn: client is not enabled, client UUID={}", clientId);
                throw new ForbiddenException("Your profile is disabled");
            }

            Message message = messageRepository.findByUuid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
            if(!message.getAuthor().getUuid().equals(clientId)) {
                log.warn("Delete message warn: client is not author, message UUID={}, client UUID={}", id, clientId);
                throw new ForbiddenException("You cannot delete this message");
            }
            chatMemberRepository.findByChat_UuidAndUser_Uuid(message.getChat().getUuid(), clientId)
                    .orElseThrow(() -> {
                        log.warn("Delete message warn: client is not a member of chat, message UUID={}, client UUID={}", id, clientId);
                        return new ForbiddenException("You are not a member of this chat");
                    });

            StompResponse<UUID> stompResponse = new StompResponse<>(stompTargetMessage, StompEvent.DELETED, message.getChat().getUuid(), message.getUuid());
            sendToChatMembers(message.getChat().getUuid(), stompResponse);
            messageRepository.delete(message);
        } catch (ResourceNotFoundException | ServiceException | ForbiddenException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Database error during deleting message, message UUID={}", id, ex);
            throw new ServiceException("Database error during deleting message", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during deleting message, message UUID={}", id, ex);
            throw new ServiceException("Unexpected error deleting message", ex);
        }
    }

    /**
     * Отправляет STOMP-событие в личный топик {@code /user/topic/messages} каждого
     * текущего участника чата. Список участников читается из базы в момент отправки,
     * поэтому вышедшие из чата пользователи событие не получают.
     *
     * @param chatUuid UUID чата
     * @param event событие для отправки
     */
    private void sendToChatMembers(UUID chatUuid, StompResponse<?> event) {
        for (ChatMember member : chatMemberRepository.findByChat_Uuid(chatUuid)) {
            messagingTemplate.convertAndSendToUser(member.getUser().getEmail(), "/topic/messages", event);
        }
    }
}
