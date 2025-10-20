package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChangePriorityRequestDto {
  @JsonProperty("priority")
  private PriorityEnum priority;
}
