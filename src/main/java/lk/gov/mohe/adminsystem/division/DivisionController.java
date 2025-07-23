package lk.gov.mohe.adminsystem.division;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionRepository divisionRepository;

    @GetMapping("/divisions")
    public ResponseEntity<List<Division>> getAllDivisions() {
        List<Division> divisions = divisionRepository.findAll();
        return ResponseEntity.ok(divisions);
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