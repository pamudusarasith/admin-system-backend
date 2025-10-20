package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaper;
import lk.gov.mohe.adminsystem.user.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "cabinet_decisions")
public class CabinetDecision {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "paper_id", nullable = false)
  private CabinetPaper paper;

  @NotNull
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "decision_type", columnDefinition = "decision_type_enum not null")
  private DecisionTypeEnum decisionType;

  @NotNull
  @Column(name = "decision_text", nullable = false, length = Integer.MAX_VALUE)
  private String decisionText;

  @NotNull
  @Column(name = "decision_date", nullable = false)
  private LocalDate decisionDate;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "recorded_by_user_id", nullable = false)
  private User recordedByUser;

  @Column(name = "created_at")
  private Instant createdAt;

  @PrePersist
  public void onCreate() {
    createdAt = Instant.now();
  }
}
