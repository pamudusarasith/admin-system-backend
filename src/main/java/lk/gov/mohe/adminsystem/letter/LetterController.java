package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LetterController {
    private final LetterRepository letterRepository;

    @GetMapping("/letters")
    public ResponseEntity<PaginatedResponse> getLetters(
        @RequestParam(name = "p", required = false, defaultValue = "0") Integer page,
        @RequestParam(name = "ipp", required = false, defaultValue = "10") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Letter> letterPage = letterRepository.findAll(pageable);
        List<Letter> letters = letterPage.getContent();
        PaginatedResponse response = new PaginatedResponse(
            letters,
            page,
            letterPage.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/letters")
    public ResponseEntity<String> createLetter(@Valid @RequestBody CreateLetterRequest request) {
        Letter letter = new Letter();
        letter.setReference(request.reference());
        letter.setSenderDetails(request.senderDetails().toMap());
        letter.setSentDate(request.sentDate() != null ?
            LocalDate.parse(request.sentDate()) : null);
        letter.setReceivedDate(LocalDate.parse(request.receivedDate()));
        letter.setModeOfArrival(request.modeOfArrival());
        letter.setSubject(request.subject());
        letter.setContent(request.content());
        letter.setPriority(request.priority());
        letter.setStatus(StatusEnum.NEW);
        letterRepository.save(letter);
        return new ResponseEntity<>("", HttpStatus.CREATED);
    }

    public record CreateLetterRequest(
        @NotEmpty
        String reference,

        @Valid
        @NotNull
        @JsonProperty("sender_details")
        SenderDetails senderDetails,

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

    public record SenderDetails(
        @NotBlank
        String name,

        String address,

        String email,

        String phoneNumber
    ) {
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("address", address);
            map.put("email", email);
            map.put("phone_number", phoneNumber);
            return map;
        }
    }
}
