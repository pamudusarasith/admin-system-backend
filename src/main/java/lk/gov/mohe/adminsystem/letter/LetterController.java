package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.OK)
    public Response<Page<LetterDto>> getLetters(
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        Page<LetterDto> letters = letterService.getLetters(page, pageSize);
        return new Response<>(letters);
    }

    @GetMapping("/letters/{id}")
    @PreAuthorize("hasAuthority('letter:read')")
    @ResponseStatus(HttpStatus.OK)
    public Response<LetterDto> getLetterById(@PathVariable Integer id) {
        LetterDto letter = letterService.getLetterById(id);
        return new Response<>(letter);
    }

    @PostMapping(value = "/letters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<?>> createLetter(
        @Valid @RequestPart("details") CreateOrUpdateLetterRequestDto request,
        @RequestPart(value = "attachments", required = false) MultipartFile[] attachments
    ) {
        Letter letter = letterService.createLetter(request, attachments);
        return ResponseEntity
            .created(URI.create("/letters/" + letter.getId()))
            .body(new Response<>("Letter created successfully"));
    }

    @PutMapping("/letters/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Response<?> updateLetter(
        @PathVariable Integer id,
        @Valid @RequestBody CreateOrUpdateLetterRequestDto request
    ) {
        letterService.updateLetter(id, request);
        return new Response<>("Letter updated successfully");
    }
}
