package lk.gov.mohe.adminsystem.permission;


import lk.gov.mohe.adminsystem.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class PermissionController {

    private final PermissionRepository permissionRepository;
    private final PermissionCategoryRepository permissionCategoryRepository;
    private final PermissionService permissionService;


    @GetMapping("/permissions")
    public ApiResponse<List<MainCategoryDTO>> getPermissionHierarchy() {
        return ApiResponse.of(permissionService.getPermissionHierarchy());
    }
}
