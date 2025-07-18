package lk.gov.mohe.adminsystem.letter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LetterController {
    @GetMapping("/letters")
    public ResponseEntity<String> getLetters() {
        // This method will return a list of letters
        return ResponseEntity.ok("List of letters will be here");
    }

    @PostMapping("/letters")
    public ResponseEntity<String> createLetter() {
        // This method will create a new letter
        return ResponseEntity.ok("Letter created successfully");
    }
}
