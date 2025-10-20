package lk.gov.mohe.adminsystem.cabinetpaper;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lk.gov.mohe.adminsystem.attachment.AttachmentParent;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import lk.gov.mohe.adminsystem.user.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "cabinet_papers")
public class CabinetPaper implements AttachmentParent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Size(max = 50)
  @NotNull
  @Column(name = "reference_id", nullable = false, length = 50)
  private String referenceId;

  @Size(max = 255)
  @NotNull
  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "summary", length = Integer.MAX_VALUE)
  private String summary;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private CabinetPaperCategory category;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "cabinet_paper_status_enum not null")
  private CabinetPaperStatusEnum status;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submitted_by_user_id", nullable = false)
  private User submittedByUser;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Override
  public ParentTypeEnum getType() {
    return ParentTypeEnum.CABINET_PAPER;
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
