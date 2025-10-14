package lk.gov.mohe.adminsystem.letter;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
public class LetterSearchParams {
  private String query = "";
  private StatusEnum status;
  private PriorityEnum priority;
  private ModeOfArrivalEnum modeOfArrival;
  private String sender;
  private String receiver;
  private String assignedUser;
  private String assignedDivision;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate sentDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate sentDateFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate sentDateTo;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate receivedDate;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate receivedDateFrom;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate receivedDateTo;

  private Integer page = 0;
  private Integer pageSize = 10;
}
