package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<String> createLetter() {
        // This method will create a new letter
        return ResponseEntity.ok("Letter created successfully");
    }
}
