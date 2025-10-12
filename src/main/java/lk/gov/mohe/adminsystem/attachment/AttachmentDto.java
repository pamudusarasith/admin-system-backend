package lk.gov.mohe.adminsystem.attachment;

import java.time.Instant;

public record AttachmentDto(
    Integer id, String fileName, String fileType, String url, Instant createdAt) {}
