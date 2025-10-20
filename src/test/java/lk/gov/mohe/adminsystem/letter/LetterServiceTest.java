package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.extension.ExtendWith;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserDto;
import lk.gov.mohe.adminsystem.letter.SenderDetailsDto;
import lk.gov.mohe.adminsystem.letter.ReceiverDetailsDto;
import lk.gov.mohe.adminsystem.letter.LetterDto;
import lk.gov.mohe.adminsystem.letter.ModeOfArrivalEnum;
import lk.gov.mohe.adminsystem.letter.PriorityEnum;
import lk.gov.mohe.adminsystem.letter.StatusEnum;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LetterServiceTest {

    @Mock
    private LetterRepository letterRepository;
    @Mock
    private LetterEventRepository letterEventRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private LetterMapper letterMapper;
    @Mock
    private MinioStorageService storageService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private LetterService letterService;

    private Letter letter;
    private LetterDto letterDto;

    @BeforeEach
    void setup() {
        DivisionDto divisionDto = new DivisionDto(1, "DivisionName", "DivisionCode");
        UserDto userDto = new UserDto(
                1L, "username", "user@email.com", "Full Name",
                "1234567890", "ROLE_USER", "DivisionName", true
        );
        SenderDetailsDto senderDetails = new SenderDetailsDto(
                "Sender Name", "Sender Address", "sender@email.com", "0987654321"
        );
        ReceiverDetailsDto receiverDetails = new ReceiverDetailsDto(
                "Receiver Name", "Receiver Designation", "Receiver Division"
        );
        letterDto = new LetterDto(
                1, "REF123", senderDetails, receiverDetails, "2024-06-01", "2024-06-02",
                ModeOfArrivalEnum.EMAIL, "Test Subject", "Test Content", PriorityEnum.HIGH,
                StatusEnum.NEW, divisionDto, userDto, true, 2L,
                List.of(), List.of(), "2024-06-01T10:00:00", "2024-06-01T12:00:00"
        );
        letter = new Letter();
        letter.setId(1);
        letter.setStatus(StatusEnum.NEW);
    }

    @Test
    void testGetAccessibleLetters_AllAuthority() {
        List<Letter> letters = List.of(letter);
        Page<Letter> page = new PageImpl<>(letters, PageRequest.of(0, 10), 1);
        when(letterRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(letterMapper.toLetterDtoMin(any(Letter.class))).thenReturn(letterDto);

        Page<LetterDto> result = letterService.getAccessibleLetters(1, 1, List.of("letter:read:all"), 0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals("REF123", result.getContent().get(0).reference());
    }

    @Test
    void testGetAccessibleLetters_NoAuthority() {
        Page<LetterDto> result = letterService.getAccessibleLetters(1, 1, List.of(), 0, 10);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void testGetLetterById_AccessAllowed() {
        when(letterRepository.findById(anyInt())).thenReturn(Optional.of(letter));
        when(attachmentRepository.findByParentTypeAndParentId(eq(ParentTypeEnum.LETTER), anyInt()))
                .thenReturn(List.of());
        when(letterEventRepository.findByLetterId(anyInt())).thenReturn(List.of());
        when(letterMapper.toLetterDtoFull(any(), anyList(), anyList())).thenReturn(letterDto);

        LetterDto result = letterService.getLetterById(1, 1, 1, List.of("letter:read:all"));
        assertNotNull(result);
        assertEquals("REF123", result.reference());
    }

    @Test
    void testGetLetterById_NotFound() {
        when(letterRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () ->
                letterService.getLetterById(99, 1, 1, List.of("letter:read:all"))
        );
    }

    @Test
    void testGetLetterById_Forbidden() {
        when(letterRepository.findById(anyInt())).thenReturn(Optional.of(letter));
        // No authority, should throw forbidden
        assertThrows(ResponseStatusException.class, () ->
                letterService.getLetterById(1, 1, 1, List.of())
        );
    }

    @Test
    void testCreateLetter_Success() {
        CreateOrUpdateLetterRequestDto request = mock(CreateOrUpdateLetterRequestDto.class);
        when(request.reference()).thenReturn("REF_NEW");
        when(letterRepository.existsLetterByReference(anyString())).thenReturn(false);
        when(letterMapper.toEntity(any())).thenReturn(letter);
        when(letterRepository.save(any())).thenReturn(letter);
        when(storageService.upload(anyString(), any())).thenReturn("objectName");
        User user = new User();
        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(user);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("file.txt");
        when(file.getContentType()).thenReturn("text/plain");

        Letter result = letterService.createLetter(request, new MultipartFile[]{file});
        assertNotNull(result);
        assertEquals(StatusEnum.NEW, result.getStatus());
    }

    @Test
    void testCreateLetter_ReferenceExists() {
        CreateOrUpdateLetterRequestDto request = mock(CreateOrUpdateLetterRequestDto.class);
        when(request.reference()).thenReturn("REF123");
        when(letterRepository.existsLetterByReference(anyString())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                letterService.createLetter(request, null)
        );
    }

    @Test
    void testCreateLetter_EmptyAttachment() {
        CreateOrUpdateLetterRequestDto request = mock(CreateOrUpdateLetterRequestDto.class);
        when(request.reference()).thenReturn("REF_NEW");
        when(letterRepository.existsLetterByReference(anyString())).thenReturn(false);
        when(letterMapper.toEntity(any())).thenReturn(letter);
        when(letterRepository.save(any())).thenReturn(letter);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                letterService.createLetter(request, new MultipartFile[]{file})
        );
    }

    @Test
    void testUpdateLetter_Success() {
        CreateOrUpdateLetterRequestDto request = mock(CreateOrUpdateLetterRequestDto.class);
        when(letterRepository.findById(anyInt())).thenReturn(Optional.of(letter));
        doNothing().when(letterMapper).updateEntityFromCreateOrUpdateLetterRequestDto(any(), any());
        when(letterRepository.save(any())).thenReturn(letter);

        assertDoesNotThrow(() ->
                letterService.updateLetter(1, request, 1, 1, List.of("letter:update:all"))
        );
    }

    @Test
    void testUpdateLetter_NotFound() {
        CreateOrUpdateLetterRequestDto request = mock(CreateOrUpdateLetterRequestDto.class);
        when(letterRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                letterService.updateLetter(99, request, 1, 1, List.of("letter:update:all"))
        );
    }

    @Test
    void testUpdateLetter_Forbidden() {
        CreateOrUpdateLetterRequestDto request = mock(CreateOrUpdateLetterRequestDto.class);
        when(letterRepository.findById(anyInt())).thenReturn(Optional.of(letter));

        assertThrows(ResponseStatusException.class, () ->
                letterService.updateLetter(1, request, 1, 1, List.of())
        );
    }

}