package lk.gov.mohe.adminsystem.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionRepository permissionRepository;

  @GetMapping("/permissions")
  public ResponseEntity<?> getAllPermission() {
    return ResponseEntity.ok(permissionRepository.findAll());
  }
}
