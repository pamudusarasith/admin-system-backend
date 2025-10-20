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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

        category = new CabinetPaperCategory();
        category.setId(1);

        mockUser = new User();
        mockUser.setId(10);

        paper = new CabinetPaper();
        paper.setId(1);
        paper.setReferenceId("REF123");
        paper.setCategory(category);
        paper.setSubmittedByUser(mockUser);

        // Updated DTO with 11 arguments
        paperDto = new CabinetPaperDto(1, "REF123", "Title", "Summary", null, CabinetPaperStatusEnum.DRAFT, null, 1L, Collections.emptyList(), "2025-10-20", "2025-10-20");

        createDto = new CreateCabinetPaperRequestDto("REF456", "New Subject", "Summary text", 1, CabinetPaperStatusEnum.DRAFT);
        updateDto = new UpdateCabinetPaperRequestDto("REF123", "Updated Subject", "Updated summary", 1, CabinetPaperStatusEnum.SUBMITTED);
    }

    @Test
    void getCabinetPapers_ShouldReturnPagedDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CabinetPaper> pagedPapers = new PageImpl<>(Collections.singletonList(paper));
        when(cabinetPaperRepository.findAll(pageable)).thenReturn(pagedPapers);
        when(cabinetPaperMapper.toCabinetPaperDtoMin(any(CabinetPaper.class))).thenReturn(paperDto);

        Page<CabinetPaperDto> result = cabinetPaperService.getCabinetPapers(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cabinetPaperMapper, times(1)).toCabinetPaperDtoMin(paper);
    }

    @Test
    void getCabinetPaperById_ShouldReturnFullDto_WhenFound() {
        List<Attachment> attachments = Collections.singletonList(new Attachment());
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        when(attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.CABINET_PAPER, 1)).thenReturn(attachments);
        when(cabinetPaperMapper.toCabinetPaperDtoFull(paper, attachments)).thenReturn(paperDto);

        CabinetPaperDto result = cabinetPaperService.getCabinetPaperById(1);

        assertNotNull(result);
        verify(cabinetPaperMapper, times(1)).toCabinetPaperDtoFull(paper, attachments);
    }

    @Test
    void getCabinetPaperById_ShouldThrowNotFound_WhenNotFound() {
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cabinetPaperService.getCabinetPaperById(1));
    }

    @Test
    void createCabinetPaper_ShouldSavePaperAndAttachments_WhenValid() {
        MultipartFile[] files = {new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes())};
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(cabinetPaperMapper.toEntity(createDto)).thenReturn(paper);
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(mockUser);
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);
        when(storageService.upload(anyString(), any(MultipartFile.class))).thenReturn("minio/path/file.pdf");
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(new Attachment());

        CabinetPaper result = cabinetPaperService.createCabinetPaper(createDto, files);

        assertNotNull(result);
        verify(cabinetPaperRepository).save(paper);
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void createCabinetPaper_ShouldThrowConflict_WhenReferenceIdExists() {
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> cabinetPaperService.createCabinetPaper(createDto, null));
        verify(cabinetPaperRepository, never()).save(any());
    }

    @Test
    void createCabinetPaper_ShouldThrowUnsupportedMediaType_WhenAttachmentIsWrongType() {
        MultipartFile[] files = {new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes())};
        when(cabinetPaperRepository.existsByReferenceId(anyString())).thenReturn(false);
        when(cabinetPaperMapper.toEntity(createDto)).thenReturn(paper);
        when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(category));
        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(mockUser);
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);

        assertThrows(ResponseStatusException.class, () -> cabinetPaperService.createCabinetPaper(createDto, files));
        verify(storageService, never()).upload(anyString(), any(MultipartFile.class));
    }

    @Test
    void updateCabinetPaper_ShouldUpdatePaper_WhenValid() {
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        when(cabinetPaperRepository.save(any(CabinetPaper.class))).thenReturn(paper);

        cabinetPaperService.updateCabinetPaper(1, updateDto, null);

        verify(cabinetPaperMapper).updateEntityFromDto(updateDto, paper);
        verify(cabinetPaperRepository).save(paper);
    }

    @Test
    void deleteCabinetPaper_ShouldDeletePaperAndAttachments() throws Exception {
        Attachment mockAttachment = new Attachment();
        mockAttachment.setFilePath("minio/path/to/file.pdf");
        List<Attachment> attachments = Collections.singletonList(mockAttachment);
        when(cabinetPaperRepository.findById(1)).thenReturn(Optional.of(paper));
        when(attachmentRepository.findByParentTypeAndParentId(ParentTypeEnum.CABINET_PAPER, 1)).thenReturn(attachments);
        doNothing().when(storageService).delete(anyString());

        cabinetPaperService.deleteCabinetPaper(1);

        verify(storageService).delete("minio/path/to/file.pdf");
        verify(attachmentRepository).deleteAll(attachments);
        verify(cabinetPaperRepository).delete(paper);
    }
}