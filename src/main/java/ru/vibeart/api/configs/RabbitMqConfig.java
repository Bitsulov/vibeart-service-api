package ru.vibeart.api.configs;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация инфраструктуры RabbitMQ.
 * <p>
 * Определяет:
 * <ul>
 *   <li>четыре очереди для разных типов писем;</li>
 *   <li>обменник (exchange) типа {@link TopicExchange};</li>
 *   <li>биндинги между обменником и очередями по routing key.</li>
 * </ul>
 * </p>
 *
 * <h2>Компоненты</h2>
 * <ul>
 *   <li><b>queueVerificationCodeRegister</b> — очередь для писем с кодом подтверждения регистрации;</li>
 *   <li><b>queueVerificationCodeChangeEmail</b> — очередь для писем с кодом подтверждения смены адреса электронной почты;</li>
 *   <li><b>queueVerificationCodeChangePassword</b> — очередь для писем с кодом подтверждения пароля;</li>
 *   <li><b>queuePasswordEmail</b> — очередь для писем с временным паролем;</li>
 *   <li><b>exchange</b> — основной topic-exchange для маршрутизации сообщений.</li>
 * </ul>
 *
 * <h2>Routing Keys</h2>
 * <table border="1" cellspacing="0" cellpadding="4">
 *   <tr>
 *     <th>Очередь</th>
 *     <th>Routing Key</th>
 *     <th>Назначение</th>
 *   </tr>
 *   <tr>
 *     <td>queueVerificationCodeRegister</td>
 *     <td><code>RegisterVerificationCode.key</code></td>
 *     <td>Сообщения с кодами подтверждения регистрации</td>
 *   </tr>
 *   <tr>
 *     <td>queueVerificationCodeChangeEmail</td>
 *     <td><code>ChangeEmailVerificationCode.key</code></td>
 *     <td>Сообщения с кодами подтверждения смены адреса электронной почты</td>
 *   </tr>
 *   <tr>
 *     <td>queueVerificationCodeChangePassword</td>
 *     <td><code>ChangePasswordVerificationCode.key</code></td>
 *     <td>Сообщения с кодами подтверждения смены пароля</td>
 *   </tr>
 *   <tr>
 *     <td>queuePasswordEmail</td>
 *     <td><code>emailPassword.key</code></td>
 *     <td>Сообщения с временными паролями</td>
 *   </tr>
 * </table>
 *
 * <h2>Пояснение</h2>
 * <p>
 * Обменник {@link TopicExchange} позволяет маршрутизировать сообщения по шаблонам routing key.
 * В данном случае ключи заданы явно и соответствуют конкретным очередям.
 * </p>
 *
 * <h2>Пример отправки сообщения</h2>
 * <pre>
 * rabbitTemplate.convertAndSend("exchange", "emailPassword.key", messageJson);
 * </pre>
 *
 * <h2>Особенности</h2>
 * <ul>
 *   <li>Очереди создаются с параметром durable=true (сохраняются при перезапуске брокера);</li>
 *   <li>Exchange также сохраняется (durable=true, autoDelete=false);</li>
 * </ul>
 */
@Configuration
public class RabbitMqConfig {

    /** Имя очереди для писем с кодом подтверждения регистрации. */
    static final String REGISTER_VERIFICATION_CODE_QUEUE = "queueVerificationCodeRegister";

    /** Имя очереди для писем с кодом подтверждения смены адреса электронной почты. */
    static final String CHANGE_EMAIL_VERIFICATION_CODE_QUEUE = "queueVerificationCodeChangeEmail";

    /** Имя очереди для писем с кодом подтверждения смены пароля. */
    static final String CHANGE_PASSWORD_VERIFICATION_CODE_QUEUE = "queueVerificationCodeChangePassword";

    /** Имя очереди для писем с временным паролем. */
    static final String EMAIL_PASSWORD_QUEUE = "queuePasswordEmail";

    /** Имя основного topic-exchange. */
    static final String EXCHANGE_NAME = "exchange";

    /**
     * Очередь для писем с кодом подтверждения регистрации.
     *
     * @return экземпляр {@link Queue}
     */
    @Bean
    public Queue registerVerificationCodeQueue() {
        return new Queue(REGISTER_VERIFICATION_CODE_QUEUE, true);
    }

    /**
     * Очередь для писем с кодом подтверждения смены адреса электронной почты.
     *
     * @return экземпляр {@link Queue}
     */
    @Bean
    public Queue changeEmailVerificationCodeQueue() {
        return new Queue(CHANGE_EMAIL_VERIFICATION_CODE_QUEUE, true);
    }

    /**
     * Очередь для писем с кодом подтверждения смены адреса электронной почты.
     *
     * @return экземпляр {@link Queue}
     */
    @Bean
    public Queue changePasswordVerificationCodeQueue() {
        return new Queue(CHANGE_PASSWORD_VERIFICATION_CODE_QUEUE, true);
    }

    /**
     * Очередь для писем с временным паролем.
     *
     * @return экземпляр {@link Queue}
     */
    @Bean
    public Queue emailPasswordQueue() {
        return new Queue(EMAIL_PASSWORD_QUEUE, true);
    }

    /**
     * Обменник для маршрутизации сообщений по ключу.
     *
     * @return экземпляр {@link TopicExchange}
     */
    @Bean
    public Exchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Привязка очереди {@code queueVerificationCodeRegister} к обменнику с ключом {@code RegisterVerificationCode.key}.
     *
     * @param registerVerificationCodeQueue очередь для писем с кодом
     * @param exchange обменник
     * @return объект {@link Binding}
     */
    @Bean
    public Binding registerVerificationCodeQueueBinding(
            @Qualifier("registerVerificationCodeQueue") Queue registerVerificationCodeQueue,
            Exchange exchange
    ) {
        return BindingBuilder
                .bind(registerVerificationCodeQueue)
                .to(exchange)
                .with("RegisterVerificationCode.key")
                .noargs();
    }

    /**
     * Привязка очереди {@code queueVerificationCodeChangeEmail} к обменнику с ключом {@code ChangeEmailVerificationCode.key}.
     *
     * @param changeEmailVerificationCodeQueue очередь для писем с кодом
     * @param exchange обменник
     * @return объект {@link Binding}
     */
    @Bean
    public Binding changeEmailVerificationCodeQueueBinding(
            @Qualifier("changeEmailVerificationCodeQueue") Queue changeEmailVerificationCodeQueue,
            Exchange exchange
    ) {
        return BindingBuilder
                .bind(changeEmailVerificationCodeQueue)
                .to(exchange)
                .with("ChangeEmailVerificationCode.key")
                .noargs();
    }

    /**
     * Привязка очереди {@code queueVerificationCodeChangePassword} к обменнику с ключом {@code ChangePasswordVerificationCode.key}.
     *
     * @param changePasswordVerificationCodeQueue очередь для писем с кодом
     * @param exchange обменник
     * @return объект {@link Binding}
     */
    @Bean
    public Binding changePasswordVerificationCodeQueueBinding(
            @Qualifier("changePasswordVerificationCodeQueue") Queue changePasswordVerificationCodeQueue,
            Exchange exchange
    ) {
        return BindingBuilder
                .bind(changePasswordVerificationCodeQueue)
                .to(exchange)
                .with("ChangePasswordVerificationCode.key")
                .noargs();
    }

    /**
     * Привязка очереди {@code emailPasswordQueue} к обменнику с ключом {@code emailPassword.key}.
     *
     * @param emailPasswordQueue очередь для писем с паролем
     * @param exchange обменник
     * @return объект {@link Binding}
     */
    @Bean
    public Binding emailPasswordQueueBinding(
            @Qualifier("emailPasswordQueue") Queue emailPasswordQueue,
            Exchange exchange
    ) {
        return BindingBuilder
                .bind(emailPasswordQueue)
                .to(exchange)
                .with("emailPassword.key")
                .noargs();
    }
}
