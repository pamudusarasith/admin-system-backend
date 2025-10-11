package lk.gov.mohe.adminsystem.permission;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor

public class PermissionController {

    private final PermissionRepository permissionRepository;
    private final PermissionCategoryRepository permissionCategoryRepository;
    private final PermissionService permissionService;


    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissionHierarchy() {
        return ResponseEntity.ok(permissionService.getPermissionHierarchy());
    }
}
