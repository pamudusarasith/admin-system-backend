package lk.gov.mohe.adminsystem.attachment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "attachments")
public class Attachment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "parent_type", nullable = false)
  private ParentTypeEnum parentType;

  @NotNull
  @Column(name = "parent_id", nullable = false)
  private Integer parentId;

  @Size(max = 255)
  @NotNull
  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Size(max = 255)
  @NotNull
  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Size(max = 50)
  @Column(name = "file_type", length = 50)
  private String fileType;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  public void attachToParent(AttachmentParent parent) {
    parentType = parent.getType();
    parentId = parent.getId();
  }

  @PrePersist
  public void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = Instant.now();
  }
}
