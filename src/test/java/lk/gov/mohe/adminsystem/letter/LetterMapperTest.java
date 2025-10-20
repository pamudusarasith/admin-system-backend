package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.attachment.AttachmentMapper;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.attachment.ParentTypeEnum;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.division.DivisionMapper;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserDto;
import lk.gov.mohe.adminsystem.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Use SpringExtension to create a small application context for the mapper
@ExtendWith(SpringExtension.class)
// Import the TestConfiguration which provides the beans needed for this test
@Import(LetterMapperTest.MapperTestConfig.class)
class LetterMapperTest {

    // This is the configuration that sets up our test environment
    @TestConfiguration
    static class MapperTestConfig {
        // Provide the real implementation of LetterMapper
        @Import(LetterMapperImpl.class)
        static class TestMappers {}

        // Mock all the dependencies that LetterMapper uses
        @MockBean
        private UserMapper userMapper;
        @MockBean
        private DivisionMapper divisionMapper;
        @MockBean
        private AttachmentMapper attachmentMapper;
        @MockBean
        private AttachmentRepository attachmentRepository;
    }

    // Inject the real mapper instance from the test context
    @Autowired
    private LetterMapper letterMapper;

    // Inject the mocks so we can define their behavior
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DivisionMapper divisionMapper;

    private CreateOrUpdateLetterRequestDto createDto;
    private Letter letter;
    private User assignedUser;

    @BeforeEach
    void setUp() {
        // Setup common test data
        SenderDetailsDto senderDetails = new SenderDetailsDto("Sender", "Addr", "e@mail.com", "123");
        createDto = new CreateOrUpdateLetterRequestDto("REF123", senderDetails, null, "2025-01-01", "2025-01-02", ModeOfArrivalEnum.EMAIL, "Subject", "Content", PriorityEnum.NORMAL);

        assignedUser = new User();
        assignedUser.setId(100);

        letter = new Letter();
        letter.setId(1);
        letter.setReference("REF123");
        letter.setAssignedUser(assignedUser);
    }

    @Test
    void toEntity_ShouldMapDtoToLetter() {
        // When
        Letter result = letterMapper.toEntity(createDto);

        // Then
        assertNotNull(result);
        assertEquals("REF123", result.getReference());
        assertEquals("Subject", result.getSubject());
        // Verify ignored fields are null
        assertNull(result.getId());
        assertNull(result.getStatus());
    }

    @Test
    void updateEntityFromCreateOrUpdateLetterRequestDto_ShouldUpdateTargetLetter() {
        // Given
        Letter targetLetter = new Letter();
        targetLetter.setId(5); // This ID should not be changed
        targetLetter.setStatus(StatusEnum.NEW); // This status should not be changed

        // When
        letterMapper.updateEntityFromCreateOrUpdateLetterRequestDto(createDto, targetLetter);

        // Then
        assertEquals("REF123", targetLetter.getReference());
        assertEquals("Subject", targetLetter.getSubject());
        // Verify ignored fields remain untouched
        assertEquals(5, targetLetter.getId());
        assertEquals(StatusEnum.NEW, targetLetter.getStatus());
    }

    @Test
    void toLetterDtoMin_ShouldMapLetterAndCountAttachments() {
        // Given
        UserDto mockUserDto = new UserDto(100, "User", null, null, null, true);
        // Mock the dependency calls
        when(attachmentRepository.countByParentIdAndParentType(1, ParentTypeEnum.LETTER)).thenReturn(5L);
        when(userMapper.toUserDtoMin(assignedUser)).thenReturn(mockUserDto);

        // When
        LetterDto result = letterMapper.toLetterDtoMin(letter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals(5L, result.noOfAttachments());
        assertEquals(mockUserDto, result.assignedUser());
        // Verify ignored fields are null
        assertNull(result.attachments());
        assertNull(result.events());
    }

    @Test
    void toEventDetailsDto_ShouldMapAllPresentFields() {
        // Given
        User user = new User();
        user.setId(1);
        Division division = new Division();
        division.setId(2);
        Attachment attachment = new Attachment();
        attachment.setId(3);

        UserDto userDto = new UserDto(1, "Test", null, null, null, true);
        DivisionDto divisionDto = new DivisionDto(2, "Test Div", null);
        AttachmentDto attachmentDto = new AttachmentDto(3, "file.pdf", null, null);

        // Mock the mappers that will be called inside
        when(userMapper.toUserDtoMin(any(User.class))).thenReturn(userDto);
        when(divisionMapper.toDto(any(Division.class))).thenReturn(divisionDto);
        when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(attachmentDto);

        Map<String, Object> eventDetailsMap = Map.of(
                "newStatus", "NEW",
                "newPriority", PriorityEnum.HIGH,
                "content", "This is a note.",
                "user", user,
                "division", division,
                "attachments", Collections.singletonList(attachment)
        );

        // When
        EventDetailsDto result = letterMapper.toEventDetailsDto(eventDetailsMap);

        // Then
        assertNotNull(result);
        assertEquals("NEW", result.newStatus());
        assertEquals(PriorityEnum.HIGH, result.newPriority());
        assertEquals("This is a note.", result.content());
        assertEquals(userDto, result.user());
        assertEquals(divisionDto, result.division());
        assertNotNull(result.attachments());
        assertEquals(1, result.attachments().size());
        assertEquals(attachmentDto, result.attachments().get(0));
    }

    @Test
    void toEventDetailsDto_ShouldReturnNull_WhenMapIsNull() {
        // When & Then
        assertNull(letterMapper.toEventDetailsDto(null));
    }
}