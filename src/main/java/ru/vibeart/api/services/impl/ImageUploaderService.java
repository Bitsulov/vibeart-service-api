package ru.vibeart.api.services.impl;

import io.minio.*;
import net.coobird.thumbnailator.Thumbnails;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Сервис для загрузки и удаления изображений в облачное хранилище MinIO.
 * <p>
 * Реализует функционал:
 * <ul>
 *     <li>создание бакета (если не существует);</li>
 *     <li>загрузка и сжатие изображений перед отправкой;</li>
 *     <li>удаление изображений по URL.</li>
 * </ul>
 * </p>
 *
 * <h2>Назначение</h2>
 * <p>
 * Используется для обработки пользовательских фотографий, аватаров и других изображений.
 * Все изображения сохраняются в бакете {@code ru-vibeart-images} MinIO.
 * </p>
 *
 * <h2>Зависимости</h2>
 * <ul>
 *   <li>{@link MinioClient} — клиент для работы с MinIO API;</li>
 *   <li>{@link Thumbnails} — библиотека для сжатия и преобразования изображений;</li>
 *   <li>{@link MultipartFile} — входные данные от клиента (загружаемый файл).</li>
 * </ul>
 *
 * <h2>Пример конфигурации MinIO</h2>
 * <pre>
 * minio:
 *   endpoint: http://localhost:9000
 *   access-key: admin
 *   secret-key: password123
 * </pre>
 */
@Service
public class ImageUploaderService {

    /**
     * Клиент MinIO для взаимодействия с хранилищем.
     */
    private final MinioClient minioClient;

    /**
     * Название бакета, в котором хранятся изображения.
     */
    private final String bucketName;

    /**
     * Публичный endpoint MinIO для формирования ссылок.
     */
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * Конструктор. Проверяет наличие бакета и создаёт его при необходимости.
     *
     * @param minioClient клиент MinIO
     */
    public ImageUploaderService(
            MinioClient minioClient,
            @Value("${minio.bucket}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;

        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка подключения к объектному хранилищу: " + e.getMessage(), e);
        }
    }

    /**
     * Загружает изображение в хранилище MinIO.
     * <p>
     * Сжимает изображение с помощью {@link Thumbnails}, преобразует в JPEG
     * и сохраняет под уникальным именем.
     * </p>
     *
     * @param file входной файл изображения
     * @return публичный URL загруженного файла
     * @throws IOException если файл не удалось прочитать
     * @throws ServiceException если произошла ошибка при загрузке в MinIO
     */
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            // Чтение картинки из переданного файла
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IllegalArgumentException("Invalid image file");
            }

            // Создание потока байтов
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            // Сжатие изображения до 70% качества
            Thumbnails.of(originalImage)
                    .size(originalImage.getWidth(), originalImage.getHeight())
                    .outputFormat("jpg")
                    .outputQuality(0.7)
                    .toOutputStream(os);

            // Преобразование в массив байтов
            byte[] imageData = os.toByteArray();

            String objectName = "client_" + UUID.randomUUID() + ".jpg";

            // Загрузка файла в MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(imageData), (long) imageData.length, -1L)
                            .contentType("image/jpeg")
                            .build());

            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            throw new ServiceException("Image upload failed", e);
        }
    }

    /**
     * Удаляет изображение из MinIO по его URL.
     *
     * @param imageUrl URL изображения
     * @throws ServiceException если не удалось удалить файл
     */
    public void deleteImage(String imageUrl) {
        try {
            String objectName = extractObjectNameFromUrl(imageUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new ServiceException("Image deletion failed", e);
        }
    }

    /**
     * Извлекает имя объекта из полного URL.
     *
     * @param imageUrl полный URL изображения
     * @return имя объекта в бакете MinIO
     */
    private String extractObjectNameFromUrl(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);

            // Определение пути без домена
            String path = uri.getPath();

            // Выделение имени объекта без названия бакета и двух символов слэша
            return path.substring(bucketName.length() + 2);
        } catch (Exception e) {
            throw new IllegalArgumentException("Incorrect format of URL: " + imageUrl, e);
        }
    }
}
