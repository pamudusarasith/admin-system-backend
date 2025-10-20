package lk.gov.mohe.adminsystem.cabinetpaper;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
public class CabinetPaperSearchParams {
  private String query = "";
  private CabinetPaperStatusEnum status;
  private String categoryName;
  private String submittedByUser;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant createdAtFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant createdAtTo;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant updatedAtFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Instant updatedAtTo;

  private Integer page = 0;
  private Integer pageSize = 10;
}
