package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LetterController {
    private final LetterService letterService;

    @GetMapping("/letters")
    @PreAuthorize("hasAnyAuthority('letter:read:all', 'letter:read:unassigned', " +
        "'letter:read:division', 'letter:read:own')")
    public ApiResponse<List<LetterDto>> getLetters(
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize,
        Authentication authentication
    ) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Collection<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        Page<LetterDto> letterPage = letterService.getAccessibleLetters(
            jwt.getClaim("userId"),
            jwt.getClaim("divisionId"),
            authorities,
            page,
            pageSize);
        return ApiResponse.paged(letterPage);
    }

    @GetMapping("/letters/{id}")
    @PreAuthorize("hasAnyAuthority('letter:read:all', 'letter:read:unassigned', " +
        "'letter:read:division', 'letter:read:own')")
    public ApiResponse<LetterDto> getLetterById(
        @PathVariable Integer id,
        Authentication authentication
    ) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Collection<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        LetterDto letter = letterService.getLetterById(
            id,
            jwt.getClaim("userId"),
            jwt.getClaim("divisionId"),
            authorities);
        return ApiResponse.of(letter);
    }

    @PostMapping(value = "/letters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('letter:create')")
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
    @PreAuthorize("hasAnyAuthority('letter:update:all', 'letter:update:unassigned', " +
        "'letter:update:division', 'letter:update:own')")
    public ApiResponse<Void> updateLetter(
        @PathVariable Integer id,
        @Valid @RequestBody CreateOrUpdateLetterRequestDto request,
        Authentication authentication
    ) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Collection<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        letterService.updateLetter(
            id,
            request,
            jwt.getClaim("userId"),
            jwt.getClaim("divisionId"),
            authorities);
        return ApiResponse.message("Letter updated successfully");
    }
}
