package ru.vibeart.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vibeart.api.models.entities.User;
import ru.vibeart.api.models.entities.VerificationCode;
import ru.vibeart.api.models.enums.VerificationCodesType;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link VerificationCode}.
 * <p>
 * Расширяет {@link JpaRepository}, предоставляя стандартные CRUD-операции
 * (создание, чтение, обновление, удаление) и добавляет метод для поиска по имени пользователя и типу кода.
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Отвечает за доступ к данным кодов подтверждения в базе данных.
 * </p>
 *
 * <h2>Основные возможности</h2>
 * <ul>
 *   <li>{@link #findByUserAndType(User, VerificationCodesType)} — поиск пользователя по email;</li>
 * </ul>
 *
 */
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    /**
     * Ищет код подтверждения по пользователю и типу кода.
     *
     * @param user пользователь, для которого выполняется поиск
     * @param type тип кода подтверждения
     * @return {@link Optional}, содержащий найденный код подтверждения, если он существует
     */
    Optional<VerificationCode> findByUserAndType(User user, VerificationCodesType type);
}
