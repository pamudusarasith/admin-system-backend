package lk.gov.mohe.adminsystem.cabinetpaper;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategory;
import lk.gov.mohe.adminsystem.cabinetpaper.category.CabinetPaperCategoryRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CabinetPaperServiceTest {

    @Mock
    private CabinetPaperRepository cabinetPaperRepository;
    @Mock
    private CabinetPaperCategoryRepository categoryRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private MinioStorageService storageService;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private CabinetPaperMapper cabinetPaperMapper;

    @InjectMocks
    private CabinetPaperService cabinetPaperService;

    private CabinetPaper paper;
    private CabinetPaperDto paperDto;
    private CabinetPaperCategory category;
    private User mockUser;
    private CreateCabinetPaperRequestDto createDto;
    private UpdateCabinetPaperRequestDto updateDto;

    @BeforeEach
    void setUp() {
        // Use ReflectionTestUtils to inject the @Value field
        ReflectionTestUtils.setField(cabinetPaperService, "acceptedMimeTypes", Set.of("application/pdf"));

        // Setup common entities
        category = new CabinetPaperCategory();
        category.setId(1);

        mockUser = new User();
        mockUser.setId(10);

        paper = new CabinetPaper();
        paper.setId(1);
        paper.setReferenceId("REF123");
        paper.setCategory(category);
        paper.setSubmittedByUser(mockUser);

        paperDto = new CabinetPaperDto(1, "REF123", null, null, null, null, null, null);

        createDto = new CreateCabinetPaperRequestDto("REF456", "Title", 1);
        updateDto = new UpdateCabinetPaperRequestDto("REF123", "New Title", 1);
    }

    // --- GET Cabinet Papers Tests ---

    @Test
    void getCabinetPapers_ShouldReturnPagedDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<CabinetPaper> pagedPapers = new PageImpl<>(Collections.singletonList(paper));
        when(cabinetPaperRepository.findAll(pageable)).thenReturn(pagedPapers);
        when(cabinetPaperMapper.toCabinetPaperDtoMin(any(CabinetPaper.class))).thenReturn(paperDto);

        // When
        Page<CabinetPaperDto> result = cabinetPaperService.getCabinetPapers(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cabinetPaperMapper, times(1)).toCabinetPaperDtoMin(paper);
    }

    @Test
    void getCabinetPaperById_ShouldReturnFullDto_WhenFound() {
        // Given
        Attachment mockAttachment = new Attachment();
        List<Attachment> attachments = Collections.singletonList(mockAttachment);
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        when(attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.CABINET_PAPER, 1)).thenReturn(attachments);
        when(cabinetPaperMapper.toCabinetPaperDtoFull(paper, attachments)).thenReturn(paperDto);

        // When
        CabinetPaperDto result = cabinetPaperService.getCabinetPaperById(1);

        // Then
        assertNotNull(result);
        verify(cabinetPaperMapper, times(1)).toCabinetPaperDtoFull(paper, attachments);
    }

    @Test
    void getCabinetPaperById_ShouldThrowNotFound_WhenNotFound() {
        // Given
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cabinetPaperService.getCabinetPaperById(1));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // --- CREATE Cabinet Paper Tests ---

    @Test
    void createCabinetPaper_ShouldSavePaperAndAttachments_WhenValid() {
        // Given
        MultipartFile[] files = {new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes())};
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(cabinetPaperMapper.toEntity(createDto)).thenReturn(paper);
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(mockUser);
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);
        when(storageService.upload(anyString(), any(MultipartFile.class))).thenReturn("minio/path/file.pdf");
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(new Attachment());

        // When
        CabinetPaper result = cabinetPaperService.createCabinetPaper(createDto, files);

        // Then
        assertNotNull(result);
        verify(cabinetPaperRepository, times(1)).existsByReferenceId(createDto.referenceId());
        verify(cabinetPaperRepository, times(1)).save(paper);
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
        verify(storageService, times(1)).upload(anyString(), any(MultipartFile.class));
    }

    @Test
    void createCabinetPaper_ShouldThrowConflict_WhenReferenceIdExists() {
        // Given
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResponseStatusException.class,
                () -> cabinetPaperService.createCabinetPaper(createDto, null));
        verify(cabinetPaperRepository, never()).save(any());
    }

    @Test
    void createCabinetPaper_ShouldThrowNotFound_WhenCategoryNotFound() {
        // Given
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(cabinetPaperMapper.toEntity(createDto)).thenReturn(paper);
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class,
                () -> cabinetPaperService.createCabinetPaper(createDto, null));
        verify(cabinetPaperRepository, never()).save(any());
    }

    @Test
    void createCabinetPaper_ShouldThrowUnsupportedMediaType_WhenAttachmentIsWrongType() {
        // Given
        MultipartFile[] files = {new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes())};
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(cabinetPaperMapper.toEntity(createDto)).thenReturn(paper);
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(mockUser);
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cabinetPaperService.createCabinetPaper(createDto, files));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
        verify(storageService, never()).upload(anyString(), any(MultipartFile.class));
    }

    // --- UPDATE Cabinet Paper Tests ---

    @Test
    void updateCabinetPaper_ShouldUpdatePaper_WhenValid() {
        // Given
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        // Same reference ID, so no conflict check on existing ID needed.
        // Same category ID, so no category lookup needed in this case.
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);

        // When
        cabinetPaperService.updateCabinetPaper(1, updateDto, null);

        // Then
        verify(cabinetPaperMapper, times(1)).updateEntityFromDto(updateDto, paper);
        verify(cabinetPaperRepository, times(1)).save(paper);
        verify(categoryRepository, never()).findById(anyInt());
    }

    @Test
    void updateCabinetPaper_ShouldThrowConflict_WhenNewReferenceIdExists() {
        // Given
        UpdateCabinetPaperRequestDto newRefDto = new UpdateCabinetPaperRequestDto("NEW-REF", "Title", 1);
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        // New reference ID is different, AND it exists
        when(cabinetPaperRepository.existsByReferenceId("NEW-REF")).thenReturn(true);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cabinetPaperService.updateCabinetPaper(1, newRefDto, null));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(cabinetPaperRepository, never()).save(any());
    }

    @Test
    void updateCabinetPaper_ShouldUpdateCategory_WhenCategoryChanged() {
        // Given
        UpdateCabinetPaperRequestDto newCatDto = new UpdateCabinetPaperRequestDto("REF123", "Title", 2);
        CabinetPaperCategory newCategory = new CabinetPaperCategory();
        newCategory.setId(2);

        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(newCategory));
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);

        // When
        cabinetPaperService.updateCabinetPaper(1, newCatDto, null);

        // Then
        assertEquals(newCategory, paper.getCategory());
        verify(categoryRepository, times(1)).findById(2);
        verify(cabinetPaperRepository, times(1)).save(paper);
    }

    // --- DELETE Cabinet Paper Tests ---

    @Test
    void deleteCabinetPaper_ShouldDeletePaperAndAttachments() throws Exception {
        // Given
        Attachment mockAttachment = new Attachment();
        mockAttachment.setFilePath("minio/path/to/file.pdf");
        List<Attachment> attachments = Collections.singletonList(mockAttachment);
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        when(attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.CABINET_PAPER, 1)).thenReturn(attachments);
        doNothing().when(storageService).delete(anyString());

        // When
        cabinetPaperService.deleteCabinetPaper(1);

        // Then
        verify(storageService, times(1)).delete("minio/path/to/file.pdf");
        verify(attachmentRepository, times(1)).deleteAll(attachments);
        verify(cabinetPaperRepository, times(1)).delete(paper);
    }

    @Test
    void deleteCabinetPaper_ShouldThrowNotFound_WhenPaperDoesNotExist() {
        // Given
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class,
                () -> cabinetPaperService.deleteCabinetPaper(1));

        verify(attachmentRepository, never()).deleteAll(any());
        verify(cabinetPaperRepository, never()).delete(any());
    }
}