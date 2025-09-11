package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.user.UserMinDto;

import java.time.Instant;
import java.util.Map;

public record LetterEventDto(
    Integer id,
    UserMinDto user,
    EventTypeEnum eventType,
    Map<String, Object> eventDetails,
    Instant createdAt
) {
}
