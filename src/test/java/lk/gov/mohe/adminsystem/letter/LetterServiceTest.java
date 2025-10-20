package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

    //<editor-fold desc="Mocks and Injections">
    @Mock
    private LetterRepository letterRepository;
    @Mock
    private LetterEventRepository letterEventRepository;
    @Mock
    private DivisionRepository divisionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private LetterMapper letterMapper;
    //</editor-fold>

    @InjectMocks
    private LetterService letterService;

    //<editor-fold desc="Test Data Setup">
    private User currentUser;
    private Division userDivision;
    private Letter letterAssignedToUser;
    private Letter unassignedLetter;
    private CreateOrUpdateLetterRequestDto createDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(letterService, "acceptedMimeTypes", Set.of("application/pdf"));

        userDivision = new Division();
        userDivision.setId(10);

        currentUser = new User();
        currentUser.setId(100);
        currentUser.setDivision(userDivision);

        unassignedLetter = new Letter();
        unassignedLetter.setId(1);
        unassignedLetter.setReference("UNASSIGNED_REF");

        letterAssignedToUser = new Letter();
        letterAssignedToUser.setId(3);
        letterAssignedToUser.setAssignedDivision(userDivision);
        letterAssignedToUser.setAssignedUser(currentUser);

        // Corrected: Instantiating DTOs with the correct number of arguments
        SenderDetailsDto senderDetails = new SenderDetailsDto("Sender Name", "Sender Address", "sender@email.com", "011222333");
        ReceiverDetailsDto receiverDetails = new ReceiverDetailsDto("Receiver Name", "Director", "IT Division");

        createDto = new CreateOrUpdateLetterRequestDto(
                "NEW_REF",
                senderDetails,
                receiverDetails,
                "2025-10-21",
                "2025-10-22",
                ModeOfArrivalEnum.HAND_DELIVERED,
                "Test Subject",
                "Test content",
                PriorityEnum.NORMAL
        );

        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(letterEventRepository.save(any(LetterEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
    //</editor-fold>

    @Test
    void getAccessibleLetters_ShouldCallFindAll_WhenUserHasAllReadAuthority() {
        Collection<String> authorities = List.of("letter:all:read");
        when(letterRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        letterService.getAccessibleLetters(100, 10, authorities, null, 0, 10);

        verify(letterRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getLetterById_ShouldSucceed_WhenUserHasOwnAccessToOwnLetter() {
        when(letterRepository.findById(3)).thenReturn(Optional.of(letterAssignedToUser));
        Collection<String> authorities = List.of("letter:own:manage");

        assertDoesNotThrow(() -> letterService.getLetterById(3, 100, 10, authorities));
    }

    @Test
    void getLetterById_ShouldFail_WhenUserHasNoAccess() {
        when(letterRepository.findById(1)).thenReturn(Optional.of(unassignedLetter));
        Collection<String> authorities = List.of("some:other:permission");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> letterService.getLetterById(1, 100, 10, authorities));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createLetter_ShouldSaveAndCreateEvent_WhenReferenceIsUnique() {
        when(letterRepository.existsLetterByReference(createDto.reference())).thenReturn(false);
        when(letterMapper.toEntity(createDto)).thenReturn(new Letter());
        when(letterRepository.save(any(Letter.class))).thenAnswer(inv -> inv.getArgument(0));

        letterService.createLetter(createDto, null);

        verify(letterRepository, times(1)).save(any(Letter.class));
        verify(letterEventRepository, times(1)).save(any(LetterEvent.class));
    }

    @Test
    void createLetter_ShouldThrowConflict_WhenReferenceExists() {
        when(letterRepository.existsLetterByReference(createDto.reference())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> letterService.createLetter(createDto, null));
        verify(letterRepository, never()).save(any());
    }

    @Test
    void assignDivision_ShouldSucceed_WhenLetterIsUnassigned() {
        when(letterRepository.findById(1)).thenReturn(Optional.of(unassignedLetter));
        when(divisionRepository.findById(10)).thenReturn(Optional.of(userDivision));

        letterService.assignDivision(1, 10);

        assertEquals(userDivision, unassignedLetter.getAssignedDivision());
        assertEquals(StatusEnum.ASSIGNED_TO_DIVISION, unassignedLetter.getStatus());
        verify(letterRepository, times(1)).save(unassignedLetter);
    }

    @Test
    void assignUser_ShouldThrowBadRequest_WhenUserIsNotInSameDivision() {
        Letter letterInDivision = new Letter();
        letterInDivision.setId(2);
        letterInDivision.setAssignedDivision(userDivision);

        User userFromAnotherDivision = new User();
        userFromAnotherDivision.setId(200);
        Division anotherDivision = new Division();
        anotherDivision.setId(20);
        userFromAnotherDivision.setDivision(anotherDivision);

        when(letterRepository.findById(2)).thenReturn(Optional.of(letterInDivision));
        when(userRepository.findById(200)).thenReturn(Optional.of(userFromAnotherDivision));

        assertThrows(ResponseStatusException.class, () -> letterService.assignUser(2, 200));
    }

    @Test
    void acceptLetter_ShouldSucceed_WhenStatusIsPendingAndUserIsCorrect() {
        letterAssignedToUser.setStatus(StatusEnum.PENDING_ACCEPTANCE);
        when(letterRepository.findById(3)).thenReturn(Optional.of(letterAssignedToUser));

        letterService.acceptLetter(3, 100);

        assertEquals(StatusEnum.ASSIGNED_TO_OFFICER, letterAssignedToUser.getStatus());
        assertTrue(letterAssignedToUser.getIsAcceptedByUser());
        verify(letterRepository, times(1)).save(letterAssignedToUser);
    }
}