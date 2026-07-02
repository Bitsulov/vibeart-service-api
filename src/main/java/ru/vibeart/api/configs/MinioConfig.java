package ru.vibeart.api.configs;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация подключения к MinIO — объектному хранилищу, совместимому с Amazon S3.
 * <p>
 * Определяет бин {@link MinioClient}, который используется для работы с файлами:
 * загрузка, скачивание, удаление, создание бакетов и т.д.
 * </p>
 *
 * <h2>Параметры конфигурации</h2>
 * Значения читаются из {@code application.yml} или переменных окружения:
 * <pre>
 * minio:
 *   endpoint: http://localhost:9000
 *   access-key: minioadmin
 *   secret-key: minioadmin
 * </pre>
 *
 * <h2>Пример использования</h2>
 * <pre>{@code
 * @Service
 * public class FileService {
 *     private final MinioClient minioClient;
 *
 *     public FileService(MinioClient minioClient) {
 *         this.minioClient = minioClient;
 *     }
 *
 *     public void uploadFile(String bucket, String objectName, InputStream data) {
 *         minioClient.putObject(
 *             PutObjectArgs.builder()
 *                 .bucket(bucket)
 *                 .object(objectName)
 *                 .stream(data, -1, 10485760)
 *                 .contentType("image/png")
 *                 .build()
 *         );
 *     }
 * }
 * }</pre>
 */
@Configuration
public class MinioConfig {

    /** URL MinIO-сервера (например, http://localhost:9000). */
    @Value("${minio.endpoint}")
    private String endpoint;

    /** Access key для авторизации в MinIO. */
    @Value("${minio.access-key}")
    private String accessKey;

    /** Secret key для авторизации в MinIO. */
    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * Создаёт и регистрирует бин {@link MinioClient}, используемый для всех операций с MinIO.
     *
     * @return настроенный экземпляр {@link MinioClient}
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
