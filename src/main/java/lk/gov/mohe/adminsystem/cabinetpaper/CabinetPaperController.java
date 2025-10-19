package lk.gov.mohe.adminsystem.cabinetpaper;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CabinetPaperController {
  private final CabinetPaperService cabinetPaperService;

  @GetMapping("/cabinet-papers")
  @PreAuthorize("hasAuthority('cabinet:read')")
  public ApiResponse<List<CabinetPaperDto>> getCabinetPapers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
    Page<CabinetPaperDto> cabinetPapers = cabinetPaperService.getCabinetPapers(page, pageSize);
    return ApiResponse.paged(cabinetPapers);
  }

  @PostMapping(value = "/cabinet-papers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('cabinet:create')")
  public ResponseEntity<ApiResponse<Void>> createCabinetPaper(
      @Valid @RequestPart("details") CreateCabinetPaperRequestDto request,
      @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
    CabinetPaper paper = cabinetPaperService.createCabinetPaper(request, attachments);
    return ResponseEntity.created(URI.create("/cabinet-papers/" + paper.getId()))
        .body(ApiResponse.message("Cabinet paper created successfully"));
  }
}
