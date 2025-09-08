package lk.gov.mohe.adminsystem.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioStorageService {
    private final MinioClient minioClient;

    @Value("${custom.minio.endpoint}")
    String endpoint;
    @Value("${custom.minio.access-key}")
    String accessKey;
    @Value("${custom.minio.secret-key}")
    String secretKey;
    @Value("${custom.minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }

    public String upload(String folder, MultipartFile file) {
        try {
            ensureBucket();

            String objectName = buildObjectName(folder, file.getOriginalFilename());

            try (InputStream is = file.getInputStream()) {
                PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(is, file.getSize(), -1)
                    .build();
                minioClient.putObject(args);
            }
            return objectName;
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to MinIO", e);
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists =
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private String buildObjectName(String folder, String originalFilename) {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeFolder = (folder == null || folder.isBlank()) ? "uploads" : folder;
        String base = (originalFilename == null || originalFilename.isBlank()) ?
            "file" : originalFilename;
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
        return s.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+)|(-+$)", "");
    }
}
