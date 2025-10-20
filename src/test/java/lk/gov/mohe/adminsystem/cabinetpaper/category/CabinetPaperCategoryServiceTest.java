package lk.gov.mohe.adminsystem.cabinetpaper.category;

import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CabinetPaperCategoryServiceTest {

    @Mock
    private CabinetPaperCategoryRepository repository;
    @Mock
    private CabinetPaperRepository cabinetPaperRepository;
    @Mock
    private CabinetPaperCategoryMapper cabinetPaperCategoryMapper;

    @InjectMocks
    private CabinetPaperCategoryService service;

    private CabinetPaperCategory category;
    private CreateCabinetPaperCategoryRequestDto createDto;
    private UpdateCabinetPaperCategoryRequestDto updateDto;

    @BeforeEach
    void setUp() {
        category = new CabinetPaperCategory();
        category.setId(1);
        category.setName("Test Category");

        createDto = new CreateCabinetPaperCategoryRequestDto("New Category", "Description");
        updateDto = new UpdateCabinetPaperCategoryRequestDto("Updated Category", "Updated Description");
    }

    @Test
    void getAllCategories_ShouldCallFindAll_WhenQueryIsEmpty() {
        // Given
        Page<CabinetPaperCategory> pagedCategories = new PageImpl<>(Collections.singletonList(category));
        when(repository.findAll(any(Pageable.class))).thenReturn(pagedCategories);

        // When
        service.getAllCategories("", 0, 10);

        // Then
        verify(repository, times(1)).findAll(any(Pageable.class));
        verify(repository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void getAllCategories_ShouldCallSearch_WhenQueryIsPresent() {
        // Given
        Page<CabinetPaperCategory> pagedCategories = new PageImpl<>(Collections.singletonList(category));
        when(repository.findByNameContainingIgnoreCase(eq("test"), any(Pageable.class))).thenReturn(pagedCategories);

        // When
        service.getAllCategories("test", 0, 10);

        // Then
        verify(repository, times(1)).findByNameContainingIgnoreCase(eq("test"), any(Pageable.class));
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void createCategory_ShouldSaveCategory_WhenNameIsUnique() {
        // Given
        when(repository.existsByName(createDto.name())).thenReturn(false);
        when(cabinetPaperCategoryMapper.toEntity(createDto)).thenReturn(category);

        // When
        service.createCategory(createDto);

        // Then
        verify(repository, times(1)).save(category);
    }

    @Test
    void createCategory_ShouldThrowConflictException_WhenNameExists() {
        // Given
        when(repository.existsByName(createDto.name())).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createCategory(createDto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void updateCategory_ShouldUpdateSuccessfully_WhenNameIsUnique() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(repository.existsByNameAndIdNot(updateDto.name(), 1)).thenReturn(false);

        // When
        service.updateCategory(1, updateDto);

        // Then
        verify(cabinetPaperCategoryMapper, times(1)).updateEntityFromDto(updateDto, category);
        verify(repository, times(1)).save(category);
    }

    @Test
    void updateCategory_ShouldThrowNotFoundException_WhenCategoryDoesNotExist() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class, () -> service.updateCategory(1, updateDto));
        verify(repository, never()).save(any());
    }

    @Test
    void updateCategory_ShouldThrowConflictException_WhenNameExistsForAnotherCategory() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(repository.existsByNameAndIdNot(updateDto.name(), 1)).thenReturn(true);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> service.updateCategory(1, updateDto));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteCategory_ShouldDeleteSuccessfully_WhenNotUsed() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(cabinetPaperRepository.existsByCategory(category)).thenReturn(false);

        // When
        service.deleteCategory(1);

        // Then
        verify(repository, times(1)).deleteById(1);
    }

    @Test
    void deleteCategory_ShouldThrowConflictException_WhenCategoryIsInUse() {
        // Given
        when(repository.findById(1)).thenReturn(Optional.of(category));
        when(cabinetPaperRepository.existsByCategory(category)).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.deleteCategory(1));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).deleteById(any());
    }
}