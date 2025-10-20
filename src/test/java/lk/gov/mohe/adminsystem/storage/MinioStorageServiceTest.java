package lk.gov.mohe.adminsystem.storage;

import io.minio.*;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioStorageService minioStorageService;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // In a unit test, we must manually inject the value of the @Value field.
        ReflectionTestUtils.setField(minioStorageService, "bucket", "test-bucket");

        // Setup a common mock file for tests
        mockFile = new MockMultipartFile(
                "file",
                "test-file.pdf",
                "application/pdf",
                "This is a test file".getBytes()
        );
    }

    @Test
    void upload_ShouldSucceedAndReturnObjectName_WhenBucketExists() throws Exception {
        // Given
        // Simulate that the bucket already exists
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        // We don't need to mock putObject as it returns void.

        // When
        String objectName = minioStorageService.upload("letters", mockFile);

        // Then
        // 1. Verify that we checked for the bucket's existence but did NOT try to create it.
        verify(minioClient, times(1)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));

        // 2. Capture the arguments passed to putObject to inspect them.
        ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(1)).putObject(putObjectArgsCaptor.capture());

        // 3. Assert that the captured arguments are correct.
        PutObjectArgs capturedArgs = putObjectArgsCaptor.getValue();
        assertEquals("test-bucket", capturedArgs.bucket());
        assertEquals("application/pdf", capturedArgs.contentType());
        assertTrue(objectName.startsWith("letters/"));
        assertTrue(objectName.contains("test-file"));
        assertTrue(objectName.endsWith(".pdf"));
    }

    @Test
    void upload_ShouldCreateBucket_WhenBucketDoesNotExist() throws Exception {
        // Given
        // Simulate that the bucket does NOT exist initially
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // When
        minioStorageService.upload("letters", mockFile);

        // Then
        // Verify that we checked for the bucket AND tried to create it.
        verify(minioClient, times(1)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_ShouldThrowResponseStatusException_WhenMinioFails() throws Exception {
        // Given
        // Configure the mocked client to throw an exception when putObject is called
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        doThrow(new MinioException("Connection error")).when(minioClient).putObject(any(PutObjectArgs.class));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> minioStorageService.upload("letters", mockFile));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void getFileUrl_ShouldReturnPresignedUrl() throws Exception {
        // Given
        String objectName = "letters/2025/10/21/test-file-uuid.pdf";
        String expectedUrl = "http://minio.example.com/test-bucket/" + objectName + "?presigned-token";
        // Mock the client to return a predictable URL
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(expectedUrl);

        // When
        String actualUrl = minioStorageService.getFileUrl(objectName);

        // Then
        assertEquals(expectedUrl, actualUrl);
        verify(minioClient, times(1)).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void delete_ShouldCallRemoveObject() throws Exception {
        // Given
        String objectName = "letters/2025/10/21/test-file-uuid.pdf";

        // When
        minioStorageService.delete(objectName);

        // Then
        // Capture the arguments to verify the correct object and bucket were used.
        ArgumentCaptor<RemoveObjectArgs> removeObjectArgsCaptor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient, times(1)).removeObject(removeObjectArgsCaptor.capture());

        RemoveObjectArgs capturedArgs = removeObjectArgsCaptor.getValue();
        assertEquals("test-bucket", capturedArgs.bucket());
        assertEquals(objectName, capturedArgs.object());
    }
}