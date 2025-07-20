package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateOrUpdateLetterRequestDto(
    @NotEmpty
    String reference,

    @Valid
    @NotNull
    @JsonProperty("sender_details")
    SenderDetailsDto senderDetails,

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    @JsonProperty("sent_date")
    String sentDate,

    @NotEmpty
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    @JsonProperty("received_date")
    String receivedDate,

    @NotNull
    @JsonProperty("mode_of_arrival")
    ModeOfArrivalEnum modeOfArrival,

    @NotEmpty
    String subject,

    String content,

    @NotNull
    PriorityEnum priority
) {
}
