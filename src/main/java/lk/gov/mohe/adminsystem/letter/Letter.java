package lk.gov.mohe.adminsystem.letter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import lk.gov.mohe.adminsystem.attachment.AttachmentParent;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.user.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "letters")
public class Letter implements AttachmentParent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Size(max = 50)
  @NotNull
  @Column(name = "reference", nullable = false, length = 50)
  private String reference;

  @Column(name = "sender_details")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> senderDetails;

  @Column(name = "receiver_details")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> receiverDetails;

  @Column(name = "sent_date")
  private LocalDate sentDate;

  @Column(name = "received_date", nullable = false)
  private LocalDate receivedDate;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "mode_of_arrival", nullable = false)
  private ModeOfArrivalEnum modeOfArrival;

  @Size(max = 255)
  @NotNull
  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "content", length = Integer.MAX_VALUE)
  private String content;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(
      name = "priority",
      nullable = false,
      columnDefinition = "priority_enum DEFAULT 'NORMAL' NOT NULL")
  private PriorityEnum priority;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", nullable = false)
  private StatusEnum status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_division_id")
  private Division assignedDivision;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_user_id")
  private User assignedUser;

  @Column(name = "is_accepted_by_user", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private Boolean isAcceptedByUser;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public ParentTypeEnum getType() {
    return ParentTypeEnum.LETTER;
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
