package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lk.gov.mohe.adminsystem.user.UserDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CabinetDecisionDto(
    Integer id,
    Integer paperId,
    DecisionTypeEnum decisionType,
    String decisionText,
    LocalDate decisionDate,
    UserDto recordedByUser,
    String createdAt) {}
