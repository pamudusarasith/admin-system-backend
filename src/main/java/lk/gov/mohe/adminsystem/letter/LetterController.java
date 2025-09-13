package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    @GetMapping("/letters")
    public ResponseEntity<PaginatedResponse<LetterDto>> getLetters(
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        PaginatedResponse<LetterDto> response = letterService.getLetters(page
            , pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/letters/{id}")
    @PreAuthorize("hasAuthority('letter:read')")
    public ResponseEntity<LetterDto> getLetterById(@PathVariable Integer id) {
        LetterDto letter = letterService.getLetterById(id);
        return ResponseEntity.ok(letter);
    }

    @PostMapping(value = "/letters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createLetter(
        @Valid @RequestPart("details") CreateOrUpdateLetterRequestDto request,
        @RequestPart(value = "attachments", required = false) MultipartFile[] attachments
    ) {
        Letter letter = letterService.createLetter(request, attachments);
        return ResponseEntity.created(URI.create("/letters/" + letter.getId())).build();
    }

    @PutMapping("/letters/{id}")
    public ResponseEntity<String> updateLetter(
        @PathVariable Integer id,
        @Valid @RequestBody CreateOrUpdateLetterRequestDto request
    ) {
        letterService.updateLetter(id, request);
        return ResponseEntity.ok("");
    }
}
