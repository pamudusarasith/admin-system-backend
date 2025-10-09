package lk.gov.mohe.adminsystem.division;

import jakarta.validation.Valid;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionRepository divisionRepository;
    private final DivisionService divisionService;

    @GetMapping("/divisions")
    // Searching divisions is required for assigning letters to divisions
    @PreAuthorize("hasAnyAuthority('division:read', 'letter:assign:division')")
    public ApiResponse<List<DivisionDto>> getDivisions(
        @RequestParam(defaultValue = "") String query,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<DivisionDto> divisions = divisionService.getDivisions(query, page, pageSize);
        return ApiResponse.paged(divisions);
    }

    @PostMapping("/divisions")
    public ResponseEntity<Division> createDivision(@Valid @RequestBody Division division) {
        // The ID will be auto-generated, so we don't need to set it
        Division savedDivision = divisionRepository.save(division);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDivision);
    }

    @DeleteMapping("/divisions/{id}")
    public ResponseEntity<Void> deleteDivision(@PathVariable Integer id) {
        Optional<Division> division = divisionRepository.findById(id);

        if (division.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        divisionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/divisions/{id}")
    public ResponseEntity<Division> updateDivision(@PathVariable Integer id, @Valid @RequestBody Division divisionDetails) {
        Optional<Division> optionalDivision = divisionRepository.findById(id);

        if (optionalDivision.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Division existingDivision = optionalDivision.get();
        existingDivision.setName(divisionDetails.getName());
        existingDivision.setDescription(divisionDetails.getDescription());

        Division updatedDivision = divisionRepository.save(existingDivision);
        return ResponseEntity.ok(updatedDivision);
    }

}