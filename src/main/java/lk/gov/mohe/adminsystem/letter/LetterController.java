package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    @GetMapping("/letters")
    public ResponseEntity<PaginatedResponse<Letter>> getLetters(
        @RequestParam(name = "p", required = false, defaultValue = "0") Integer page,
        @RequestParam(name = "ipp", required = false, defaultValue = "10") Integer pageSize
    ) {
        PaginatedResponse<Letter> response = letterService.getLetters(page, pageSize);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/letters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createLetter(
        @Valid @RequestPart("details") CreateOrUpdateLetterRequestDto request,
        @RequestPart("attachments") MultipartFile[] attachments
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
