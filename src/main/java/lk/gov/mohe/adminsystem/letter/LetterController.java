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
    @PreAuthorize("hasAnyAuthority('letter:all:read', 'letter:unassigned:read', 'letter:division:read',"
            + " 'letter:own:read')")
    public ApiResponse<List<LetterDto>> getLetters(
            @ModelAttribute LetterSearchParams params, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Page<LetterDto> letterPage = letterService.getAccessibleLetters(
                jwt.getClaim("userId"),
                jwt.getClaim("divisionId"),
                authorities,
                params,
                params.getPage(),
                params.getPageSize());
        return ApiResponse.paged(letterPage);
    }

    @GetMapping("/letters/{id}")
    @PreAuthorize("hasAnyAuthority('letter:all:read', 'letter:unassigned:read', 'letter:division:read',"
            + " 'letter:own:read')")
    public ApiResponse<LetterDto> getLetterById(
            @PathVariable Integer id, Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        LetterDto letter = letterService.getLetterById(
                id, jwt.getClaim("userId"), jwt.getClaim("divisionId"), authorities);
        return ApiResponse.of(letter);
    }

    @PostMapping(value = "/letters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('letter:create')")
    public ResponseEntity<ApiResponse<Void>> createLetter(
            @Valid @RequestPart("details") CreateOrUpdateLetterRequestDto request,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        Letter letter = letterService.createLetter(request, attachments);
        return ResponseEntity.created(URI.create("/letters/" + letter.getId()))
                .body(ApiResponse.message("Letter created successfully"));
    }

    @PutMapping("/letters/{id}")
    @PreAuthorize("hasAnyAuthority('letter:all:update', 'letter:unassigned:update', 'letter:division:update',"
            + " 'letter:own:update')")
    public ApiResponse<Void> updateLetter(
            @PathVariable Integer id,
            @Valid @RequestBody CreateOrUpdateLetterRequestDto request,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Collection<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        letterService.updateLetter(
                id, request, jwt.getClaim("userId"), jwt.getClaim("divisionId"), authorities);
        return ApiResponse.message("Letter updated successfully");
    }

    @PostMapping(value = "/letters/{id}/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('letter:all:add:note', 'letter:unassigned:add:note','letter:division:add:note',"
            + "'letter:own:add:note')")
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
                id, content, attachments, jwt.getClaim("userId"), jwt.getClaim("divisionId"),
                authorities);
        return ApiResponse.message("Note added successfully");
    }

    @PutMapping("/letters/{letterId}/division")
    @PreAuthorize("hasAuthority('letter:assign:division')")
    public ApiResponse<Void> assignDivision(
            @PathVariable Integer letterId, @Valid @RequestBody AssignDivisionRequestDto request) {
        letterService.assignDivision(letterId, request.divisionId());
        return ApiResponse.message("Division assigned successfully");
    }

    @PutMapping("/letters/{letterId}/user")
    @PreAuthorize("hasAuthority('letter:assign:user')")
    public ApiResponse<Void> assignUser(
            @PathVariable Integer letterId, @Valid @RequestBody AssignUserRequestDto request) {
        letterService.assignUser(letterId, request.userId());
        return ApiResponse.message("User assigned successfully");
    }

    @DeleteMapping("/letters/{letterId}/user")
    @PreAuthorize("hasAuthority('letter:return:from:user')")
    public ApiResponse<Void> returnFromUser(
            @PathVariable Integer letterId,
            @RequestParam(defaultValue = "") String reason,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        letterService.returnFromUser(letterId, jwt.getClaim("userId"), reason
        );
        return ApiResponse.message("User unassigned successfully");
    }

    @DeleteMapping("/letters/{letterId}/division")
    @PreAuthorize("hasAuthority('letter:return:from:division')")
    public ApiResponse<Void> returnFromDivision(
            @PathVariable Integer letterId,
            @RequestBody ReturnRequestDto dto,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        letterService.returnFromDivision(letterId, jwt.getClaim("divisionId"), dto);
        return ApiResponse.message("Letter returned from division successfully");
    }

  @PatchMapping(path = "/letters/{letterId}/user", params = "action=accept")
  public ApiResponse<Void> acceptLetter(
      @PathVariable Integer letterId, Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    letterService.acceptLetter(letterId, jwt.getClaim("userId"));
    return ApiResponse.message("Letter accepted successfully");
  }

  @PatchMapping(value = "/letters/{id}", params = "action=markComplete")
  @PreAuthorize(
      "hasAnyAuthority('letter:all:markcomplete', 'letter:unassigned:markcomplete','letter:division:markcomplete',"
          + "'letter:own:markcomplete')")
  public ApiResponse<Void> markAsComplete(@PathVariable Integer id, Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Collection<String> authorities =
        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    letterService.markAsComplete(
        id, jwt.getClaim("userId"), jwt.getClaim("divisionId"), authorities);
    return ApiResponse.message("Marked as completed successfully");
  }

  @PatchMapping(value = "/letters/{id}", params = "action=reopen")
  @PreAuthorize(
      "hasAnyAuthority('letter:all:reopen', 'letter:unassigned:reopen','letter:division:reopen',"
          + "'letter:own:reopen')")
  public ApiResponse<Void> letterReOpen(@PathVariable Integer id, Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    Collection<String> authorities =
        authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    letterService.letterReOpen(id, jwt.getClaim("userId"), jwt.getClaim("divisionId"), authorities);
    return ApiResponse.message("Letter reopened successfully");
  }

  @PostMapping(value = "/letters/{id}/reply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<Void> sendReply(
      @PathVariable Integer id,
      @RequestPart("content") @NotBlank(message = "Content is required") String content,
      @RequestPart(value = "attachments", required = false) MultipartFile[] attachments,
      Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();

    letterService.sendReply(id, content, attachments, jwt.getClaim("userId"));
    return ApiResponse.message("Reply sent successfully");
  }
}
