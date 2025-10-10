package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
        @PreAuthorize("hasAnyAuthority('letter:all:read', 'letter:unassigned:read', " +
                "'letter:division:read', 'letter:own:read')")
        public ApiResponse<List<LetterDto>> getLetters(
                @RequestParam(required = false, defaultValue = "0") Integer page,
                @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                Authentication authentication) {
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
        @PreAuthorize("hasAnyAuthority('letter:all:read', 'letter:unassigned:read', " +
                "'letter:division:read','letter:own:read')")
        public ApiResponse<LetterDto> getLetterById(
                @PathVariable Integer id,
                Authentication authentication) {
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
                @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
                Letter letter = letterService.createLetter(request, attachments);
                return ResponseEntity
                        .created(URI.create("/letters/" + letter.getId()))
                        .body(ApiResponse.message("Letter created successfully"));
        }

        @PutMapping("/letters/{id}")
        @PreAuthorize("hasAnyAuthority('letter:all:update', 'letter:unassigned:update', " +
                "'letter:division:update','letter:own:update')")
        public ApiResponse<Void> updateLetter(
                @PathVariable Integer id,
                @Valid @RequestBody CreateOrUpdateLetterRequestDto request,
                Authentication authentication) {
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

        @PostMapping(value = "/letters/{id}/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyAuthority('letter:all:add:note', 'letter:unassigned:add:note'," +
                " 'letter:division:add:note','letter:own:add:note')")
        public ApiResponse<Void> addNote(
                @PathVariable Integer id,
                @RequestPart("content") @NotBlank(message = "Content is required") String content,
                @RequestPart(value = "attachments", required = false) MultipartFile[] attachments,
                Authentication authentication) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                Collection<String> authorities = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

                letterService.addNote(
                        id,
                        content,
                        attachments,
                        jwt.getClaim("userId"),
                        jwt.getClaim("divisionId"),
                        authorities);
                return ApiResponse.message("Note added successfully");
        }

        @PutMapping("/letters/{letterId}/division")
        @PreAuthorize("hasAuthority('letter:assign:division')")
        public ApiResponse<Void> assignDivision(
                @PathVariable Integer letterId,
                @Valid @RequestBody AssignDivisionRequestDto request) {
                letterService.assignDivision(letterId, request.divisionId());
                return ApiResponse.message("Division assigned successfully");
        }

        @PutMapping("/letters/{letterId}/user")
        @PreAuthorize("hasAuthority('letter:assign:user')")
        public ApiResponse<Void> assignUser(
                @PathVariable Integer letterId,
                @Valid @RequestBody AssignUserRequestDto request) {
                letterService.assignUser(letterId, request.userId());
                return ApiResponse.message("User assigned successfully");
        }
}
