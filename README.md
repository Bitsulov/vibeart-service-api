# VibeArt — Основной сервис API

Основной сервис REST API, реализующий бизнес-логику и управление данными приложения.

> Этот репозиторий является подмодулем основного репозитория [VibeArt](https://github.com/Bitsulov/VibeArt.git), который запускается через Docker Compose.

## Требования

- **JDK 25** — среда разработки Java, необходима для сборки и запуска сервиса
- **PostgreSQL** — реляционная база данных для хранения данных приложения
- **RabbitMQ** — брокер сообщений для взаимодействия с другими сервисами
- **[email-service](https://github.com/Bitsulov/vibeart-service-email)** - сервис отправки почтовых писем
- **Minio** - объектное хранилище (или любое другое S3-совместимое хранилище)

## Быстрый старт

Сервис рассчитан на запуск через Docker Compose из основного репозитория. Для локального запуска скопируйте `.env.example` в `.env` и заполните переменные:

```bash
cp .env.example .env
```

| Переменная           | Описание                                                  |
|----------------------|-----------------------------------------------------------|
| `DB_URL`             | Хост и имя базы данных, например `localhost:5432/vibeart` |
| `DB_USERNAME`        | Имя пользователя PostgreSQL                               |
| `DB_PASSWORD`        | Пароль PostgreSQL                                         |
| `RABBIT_MQ_HOST`     | Хост RabbitMQ                                             |
| `RABBIT_MQ_USERNAME` | Имя пользователя RabbitMQ                                 |
| `RABBIT_MQ_PASSWORD` | Пароль RabbitMQ                                           |
| `RABBIT_MQ_VHOST`    | Виртуальный хост RabbitMQ, обычно `/`                     |
| `JWT_SECRET_KEY`     | Секретный ключ для подписи JWT-токенов                    |
| `JWT_ACCESS_TIME`    | Время жизни access-токена в миллисекундах                 |
| `JWT_REFRESH_TIME`   | Время жизни refresh-токена в миллисекундах                |
| `MINIO_URL`          | Адрес объектного хранилища                                |
| `MINIO_USERNAME`     | Имя пользователя S3 хранилища                             |
| `MINIO_PASSWORD`     | Пароль для доступа к хранилищу                            |
| `MINIO_BUCKETNAME`   | Название бакета, в котором хранятся изображения           |

```bash
./gradlew bootRun
```

## Команды

| Команда                                   | Описание                       |
|-------------------------------------------|--------------------------------|
| `./gradlew bootRun`                       | Запуск приложения              |
| `./gradlew build`                         | Сборка проекта                 |
| `./gradlew build -x test`                 | Сборка без тестов              |
| `./gradlew test`                          | Запуск всех тестов             |
| `./gradlew test --tests "ClassName"`      | Запуск одного тестового класса |

## Стек технологий

- [Java 25](https://openjdk.org/) + [Spring Boot 4.0](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) / Hibernate — работа с базой данных
- [PostgreSQL](https://www.postgresql.org/) — база данных
- [Spring AMQP](https://spring.io/projects/spring-amqp) — отправка сообщений в RabbitMQ
- [Minio](https://www.min.io/blog/aistor-overview) - Объектное хранилище, совместимое с S3
- [JWT (jjwt)](https://github.com/jwtk/jjwt) — аутентификация на основе токенов
- [Springdoc OpenAPI](https://springdoc.org/) — автоматическая генерация документации API и Swagger UI
- [JUnit 5](https://junit.org/junit5/) — модульные и интеграционные тесты

## Архитектура

| Пакет           | Назначение                                    |
|-----------------|-----------------------------------------------|
| `configs/`      | Набор конфигураций приложения                 |
| `controllers/`  | Контроллеры для обработки HTTP-запросов       |
| `models/`       | Модели данных приложения                      |
| `dtos/`         | Модели запросов и ответов API                 |
| `exceptions/`   | Исключения и глобальный обработчик ошибок     |
| `repositories/` | Репозитории для взаимодействия с базой данных |
| `security/`     | Конфигурация и логика безопасности            |
| `services/`     | Бизнес-логика                                 |
| `utils/`        | Вспомогательные утилиты                       |

## Docker

Проект использует многоэтапный Dockerfile:

- **Build** — сборка jar-файла через Gradle
- **Run** — запуск на минимальном JRE-образе

## Ссылки

- [VibeArt](https://github.com/Bitsulov/VibeArt) — основной репозиторий
- [VibeArt — Клиентская часть](https://github.com/Bitsulov/vibeart-web-frontend) — клиентское веб-приложение
- [VibeArt — Сервис отправки писем](https://github.com/Bitsulov/vibeart-service-email) — сервис отправки электронных писем
