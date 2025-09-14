package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    @GetMapping("/letters")
    public ApiResponse<List<LetterDto>> getLetters(
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        Page<LetterDto> letters = letterService.getLetters(page, pageSize);
        return ApiResponse.paged(letters);
    }

    @GetMapping("/letters/{id}")
    @PreAuthorize("hasAuthority('letter:read')")
    public ApiResponse<LetterDto> getLetterById(@PathVariable Integer id) {
        LetterDto letter = letterService.getLetterById(id);
        return ApiResponse.of(letter);
    }

    @PostMapping(value = "/letters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createLetter(
        @Valid @RequestPart("details") CreateOrUpdateLetterRequestDto request,
        @RequestPart(value = "attachments", required = false) MultipartFile[] attachments
    ) {
        Letter letter = letterService.createLetter(request, attachments);
        return ResponseEntity
            .created(URI.create("/letters/" + letter.getId()))
            .body(ApiResponse.message("Letter created successfully"));
    }

    @PutMapping("/letters/{id}")
    public ApiResponse<Void> updateLetter(
        @PathVariable Integer id,
        @Valid @RequestBody CreateOrUpdateLetterRequestDto request
    ) {
        letterService.updateLetter(id, request);
        return ApiResponse.message("Letter updated successfully");
    }
}
