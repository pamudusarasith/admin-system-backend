package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/letters")
    public ResponseEntity<String> createLetter(@Valid @RequestBody CreateLetterRequestDto request) {
        Letter letter = letterService.createLetter(request);
        return ResponseEntity.created(URI.create("/letters/" + letter.getId())).build();
    }
}
