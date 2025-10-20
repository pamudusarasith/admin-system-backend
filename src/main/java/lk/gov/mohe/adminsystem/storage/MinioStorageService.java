package lk.gov.mohe.adminsystem.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {
  private static final int DEFAULT_EXPIRY_SECONDS = 24 * 3600; // Default to 24 hours
  private final MinioClient minioClient;

  @Value("${custom.minio.bucket}")
  private String bucket;

  public String upload(String folder, MultipartFile file) {
    try {
      ensureBucket();

      String objectName = buildObjectName(folder, file.getOriginalFilename());

      try (InputStream is = file.getInputStream()) {
        PutObjectArgs args =
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .contentType(file.getContentType())
                .stream(is, file.getSize(), -1)
                .build();
        minioClient.putObject(args);
      }
      return objectName;
    } catch (Exception e) {
      log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
    }
  }

  public String getFileUrl(String objectName) {
    return getFileUrl(objectName, DEFAULT_EXPIRY_SECONDS);
  }

  public String getFileUrl(String objectName, int expiresInSeconds) {
    try {
      return minioClient.getPresignedObjectUrl(
          io.minio.GetPresignedObjectUrlArgs.builder()
              .method(io.minio.http.Method.GET)
              .bucket(bucket)
              .object(objectName)
              .expiry(expiresInSeconds)
              .build());
    } catch (Exception e) {
      log.error("Failed to generate a URL for object: {}", objectName, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate a URL");
    }
  }

  public void delete(String objectName) {
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
      log.info("Successfully deleted object: {}", objectName);
    } catch (Exception e) {
      log.error("Failed to delete object: {}", objectName, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file from storage");
    }
  }

  private void ensureBucket() throws Exception {
    boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }

  private String buildObjectName(String folder, String originalFilename) {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneId.of("UTC"));
    String date = formatter.format(Instant.now());
    String safeFolder = (folder == null || folder.isBlank()) ? "uploads" : folder;
    String base =
        (originalFilename == null || originalFilename.isBlank()) ? "file" : originalFilename;
    String ext = "";
    int dot = base.lastIndexOf('.');
    if (dot > 0 && dot < base.length() - 1) {
      ext = base.substring(dot);
      base = base.substring(0, dot);
    }
    String uuid = UUID.randomUUID().toString();
    return String.format("%s/%s/%s-%s%s", safeFolder, date, slugify(base), uuid, ext);
  }

  private String slugify(String s) {
    return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+)|(-+$)", "");
  }
}
