package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.user.UserDto;

import java.time.Instant;

public record LetterEventDto(
    Integer id,
    UserDto user,
    EventTypeEnum eventType,
    EventDetailsDto eventDetails,
    Instant createdAt
) {
}
