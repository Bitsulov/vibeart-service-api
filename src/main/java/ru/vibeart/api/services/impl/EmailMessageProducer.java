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
 *   <li>Поддерживает два типа писем:
 *       <ul>
 *         <li>письма с кодом подтверждения регистрации;</li>
 *         <li>письма с временным паролем.</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Связанные компоненты</h2>
 * <ul>
 *   <li>Очередь <b>queueVerificationCodeEmail</b> — принимает письма с кодом подтверждения;</li>
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
    public void sendVerificationEmail(String email, String verificationCode) {
        try {
            Map<String, String> message = new HashMap<>();
            message.put("email", email);
            message.put("verificationCode", verificationCode);
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend("exchange", "emailVerificationCode.key", jsonMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправляет сообщение с временным паролем в RabbitMQ.
     *
     * @param email    адрес получателя
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
