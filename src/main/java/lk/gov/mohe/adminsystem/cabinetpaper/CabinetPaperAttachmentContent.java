package lk.gov.mohe.adminsystem.cabinetpaper;

import jakarta.persistence.*;
import lk.gov.mohe.adminsystem.attachment.Attachment;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "cabinet_paper_attachment_contents")
public class CabinetPaperAttachmentContent {
  @Id
  @Column(name = "attachment_id", nullable = false)
  private Integer id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "attachment_id", nullable = false)
  private Attachment attachment;

  @Column(name = "text", length = Integer.MAX_VALUE)
  private String text;
}
