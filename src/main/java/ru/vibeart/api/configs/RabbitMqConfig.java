package ru.vibeart.api.configs;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация инфраструктуры RabbitMQ.
 * <p>
 * Определяет:
 * <ul>
 *   <li>две очереди для разных типов писем;</li>
 *   <li>обменник (exchange) типа {@link TopicExchange};</li>
 *   <li>биндинги между обменником и очередями по routing key.</li>
 * </ul>
 * </p>
 *
 * <h2>Компоненты</h2>
 * <ul>
 *   <li><b>queueVerificationCodeEmail</b> — очередь для писем с кодом подтверждения;</li>
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
 *     <td>queueVerificationCodeEmail</td>
 *     <td><code>emailVerificationCode.key</code></td>
 *     <td>Сообщения с кодами подтверждения</td>
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
 * rabbitTemplate.convertAndSend("exchange", "emailVerificationCode.key", messageJson);
 * </pre>
 *
 * <h2>Особенности</h2>
 * <ul>
 *   <li>Очереди создаются с параметром durable=false (не сохраняются при перезапуске брокера);</li>
 *   <li>Exchange также не сохраняется (durable=false, autoDelete=false);</li>
 *   <li>Для production-среды рекомендуется использовать durable=true для надёжности.</li>
 * </ul>
 */
@Configuration
public class RabbitMqConfig {

    /** Имя очереди для писем с кодом подтверждения. */
    static final String EMAIL_VERIFICATION_CODE_QUEUE = "queueVerificationCodeEmail";

    /** Имя очереди для писем с временным паролем. */
    static final String EMAIL_PASSWORD_QUEUE = "queuePasswordEmail";

    /** Имя основного topic-exchange. */
    static final String EXCHANGE_NAME = "exchange";

    /**
     * Очередь для писем с кодом подтверждения.
     *
     * @return экземпляр {@link Queue}
     */
    @Bean
    public Queue emailVerificationCodeQueue() {
        return new Queue(EMAIL_VERIFICATION_CODE_QUEUE, false);
    }

    /**
     * Очередь для писем с временным паролем.
     *
     * @return экземпляр {@link Queue}
     */
    @Bean
    public Queue emailPasswordQueue() {
        return new Queue(EMAIL_PASSWORD_QUEUE, false);
    }

    /**
     * Обменник для маршрутизации сообщений по ключу.
     *
     * @return экземпляр {@link TopicExchange}
     */
    @Bean
    public Exchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, false, false);
    }

    /**
     * Привязка очереди {@code queueVerificationCodeEmail} к обменнику с ключом {@code emailVerificationCode.key}.
     *
     * @param emailVerificationCodeQueue очередь для писем с кодом
     * @param exchange                   обменник
     * @return объект {@link Binding}
     */
    @Bean
    public Binding emailVerificationCodeQueueBinding(Queue emailVerificationCodeQueue, Exchange exchange) {
        return BindingBuilder
                .bind(emailVerificationCodeQueue)
                .to(exchange)
                .with("emailVerificationCode.key")
                .noargs();
    }

    /**
     * Привязка очереди {@code queuePasswordEmail} к обменнику с ключом {@code emailPassword.key}.
     *
     * @param emailPasswordQueue очередь для писем с паролем
     * @param exchange            обменник
     * @return объект {@link Binding}
     */
    @Bean
    public Binding emailPasswordQueueBinding(Queue emailPasswordQueue, Exchange exchange) {
        return BindingBuilder
                .bind(emailPasswordQueue)
                .to(exchange)
                .with("emailPassword.key")
                .noargs();
    }
}
