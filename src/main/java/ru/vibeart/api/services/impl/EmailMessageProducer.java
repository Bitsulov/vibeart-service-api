package ru.vibeart.api.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис-производитель сообщений для RabbitMQ, используемый
 * для отправки email-уведомлений (верификация и восстановление пароля).
 * <p>
 * Работает в связке с {@code EmailService} в отдельном микросервисе,
 * который получает и обрабатывает сообщения через очереди RabbitMQ.
 * </p>
 *
 * <h2>Назначение</h2>
 * <ul>
 *   <li>Отправляет JSON-сообщения в RabbitMQ с данными для отправки писем;</li>
 *   <li>Использует {@link RabbitTemplate} для взаимодействия с брокером сообщений;</li>
 *   <li>Поддерживает четыре типа писем:
 *       <ul>
 *         <li>Письма с кодом подтверждения регистрации;</li>
 *         <li>Письма с кодом подтверждения смены адреса электронной почты;</li>
 *         <li>Письма с кодом подтверждения смены пароля;</li>
 *         <li>Письма с временным паролем.</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Связанные компоненты</h2>
 * <ul>
 *   <li>Очередь <b>queueVerificationCodeRegister</b> — принимает письма с кодом подтверждения регистрации;</li>
 *   <li>Очередь <b>queueVerificationCodeChangeEmail</b> — принимает письма с кодом подтверждения смены адреса электронной почты;</li>
 *   <li>Очередь <b>queueVerificationCodeChangePassword</b> — принимает письма с кодом подтверждения смены пароля;</li>
 *   <li>Очередь <b>queuePasswordEmail</b> — принимает письма с паролем;</li>
 *   <li>Exchange <b>exchange</b> — маршрутизирует сообщения по routing key;</li>
 * </ul>
 */
@Service
public class EmailMessageProducer {

    /**
     * Шаблон для взаимодействия с RabbitMQ.
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param rabbitTemplate шаблон для отправки сообщений в RabbitMQ
     */
    public EmailMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Отправляет сообщение с кодом подтверждения регистрации в RabbitMQ.
     *
     * @param email адрес получателя
     * @param verificationCode код подтверждения
     */
    public void sendRegisterVerificationEmail(String email, String verificationCode, String language) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("email", email);
            message.put("verificationCode", verificationCode);
            message.put("language", language);
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("exchange", "RegisterVerificationCode.key", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение с кодом подтверждения смены адреса электронной почты в RabbitMQ.
     *
     * @param email адрес получателя
     * @param verificationCode код подтверждения
     */
    public void sendChangeEmailVerificationEmail(String email, String verificationCode, String language) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("email", email);
            message.put("verificationCode", verificationCode);
            message.put("language", language);
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("exchange", "ChangeEmailVerificationCode.key", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение с кодом подтверждения смены пароля в RabbitMQ.
     *
     * @param email адрес получателя
     * @param verificationCode код подтверждения
     */
    public void sendChangePasswordVerificationEmail(String email, String verificationCode, String language) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("email", email);
            message.put("verificationCode", verificationCode);
            message.put("language", language);
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("exchange", "ChangePasswordVerificationCode.key", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение с временным паролем в RabbitMQ.
     *
     * @param email адрес получателя
     * @param password временный пароль
     */
    public void sendPasswordEmail(String email, String password) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("email", email);
            message.put("password", password);
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("exchange", "emailPassword.key", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
