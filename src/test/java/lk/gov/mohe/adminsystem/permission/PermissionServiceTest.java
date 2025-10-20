package lk.gov.mohe.adminsystem.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionCategoryRepository permissionCategoryRepository;
    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionService permissionService;

    private PermissionCategory parentCategory, childCategory;
    private Permission parentPermission, childPermission;
    private PermissionCategoryDto parentCategoryDto, childCategoryDto;
    private PermissionDto parentPermissionDto, childPermissionDto;

    @BeforeEach
    void setUp() {
        // --- Mock Entities ---
        parentCategory = new PermissionCategory();
        parentCategory.setId(1);
        parentCategory.setName("User Management");

        childCategory = new PermissionCategory();
        childCategory.setId(2);
        childCategory.setName("User Permissions");
        childCategory.setParent(parentCategory);

        parentPermission = new Permission();
        parentPermission.setId(101);
        parentPermission.setName("user:create");
        parentPermission.setCategory(parentCategory);

        childPermission = new Permission();
        childPermission.setId(102);
        childPermission.setName("user:permission:assign");
        childPermission.setCategory(childCategory);

        // --- Corrected Mock DTOs ---
        // Use the new 4-argument constructor for PermissionDto
        parentPermissionDto = new PermissionDto(101, "user:create", "Create User", "Allows creating users");
        childPermissionDto = new PermissionDto(102, "user:permission:assign", "Assign Permissions", "Allows assigning permissions");

        // Use the no-arg constructor and setters for the PermissionCategoryDto class
        parentCategoryDto = new PermissionCategoryDto();
        parentCategoryDto.setId(1);
        parentCategoryDto.setName("User Management");
        parentCategoryDto.setPermissions(new ArrayList<>());
        parentCategoryDto.setSubCategories(new ArrayList<>());

        childCategoryDto = new PermissionCategoryDto();
        childCategoryDto.setId(2);
        childCategoryDto.setName("User Permissions");
        childCategoryDto.setPermissions(new ArrayList<>());
        childCategoryDto.setSubCategories(new ArrayList<>());
    }

    @Test
    void getPermissions_ShouldReturnCorrectHierarchy() {
        // Given
        when(permissionCategoryRepository.findAll()).thenReturn(Arrays.asList(parentCategory, childCategory));
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(parentPermission, childPermission));

        when(permissionMapper.categoryToDto(parentCategory)).thenReturn(parentCategoryDto);
        when(permissionMapper.categoryToDto(childCategory)).thenReturn(childCategoryDto);
        when(permissionMapper.permissionToDto(parentPermission)).thenReturn(parentPermissionDto);
        when(permissionMapper.permissionToDto(childPermission)).thenReturn(childPermissionDto);

        // When
        List<PermissionCategoryDto> result = permissionService.getPermissions();

        // Then
        assertEquals(1, result.size());

        PermissionCategoryDto resultParent = result.get(0);
        assertEquals("User Management", resultParent.getName());
        assertEquals(1, resultParent.getPermissions().size());
        assertEquals("user:create", resultParent.getPermissions().get(0).name());

        assertEquals(1, resultParent.getSubCategories().size());
        PermissionCategoryDto resultChild = resultParent.getSubCategories().get(0);
        assertEquals("User Permissions", resultChild.getName());
        assertEquals(1, resultChild.getPermissions().size());
        assertEquals("user:permission:assign", resultChild.getPermissions().get(0).name());
    }
}