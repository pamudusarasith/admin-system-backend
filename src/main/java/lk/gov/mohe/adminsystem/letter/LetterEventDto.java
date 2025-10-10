package lk.gov.mohe.adminsystem.letter;

import java.time.Instant;
import lk.gov.mohe.adminsystem.user.UserDto;

public record LetterEventDto(
    Integer id,
    UserDto user,
    EventTypeEnum eventType,
    EventDetailsDto eventDetails,
    Instant createdAt) {}
