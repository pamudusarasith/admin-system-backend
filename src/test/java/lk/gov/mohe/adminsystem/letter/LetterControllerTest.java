package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.AttachmentDto;
import lk.gov.mohe.adminsystem.division.DivisionDto;
import lk.gov.mohe.adminsystem.user.UserDto;
import lk.gov.mohe.adminsystem.util.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LetterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LetterService letterService;

    @InjectMocks
    private LetterController letterController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(letterController).build();
    }

    @Test
    @WithMockUser(authorities = "letter:read:all")
    void testGetLetters() throws Exception {
        DivisionDto divisionDto = new DivisionDto(1, "DivisionName", "DivisionCode");
        UserDto userDto = new UserDto(
                1L,
                "username",
                "user@email.com",
                "Full Name",
                "1234567890",
                "ROLE_USER",
                "DivisionName",
                true
        );
        AttachmentDto attachmentDto = new AttachmentDto(1, "file.txt", "path", "type", Instant.now());
        LetterEventDto eventDto = new LetterEventDto(1, userDto, EventTypeEnum.CHANGE_STATUS, Map.of("key", "value"), Instant.now());

        SenderDetailsDto senderDetails = new SenderDetailsDto(
                "Sender Name",
                "Sender Address",
                "sender@email.com",
                "0987654321"
        );
        ReceiverDetailsDto receiverDetails = new ReceiverDetailsDto(
                "Receiver Name",
                "Receiver Designation",
                "Receiver Division"
        );

        LetterDto dto = new LetterDto(
                1,
                "REF123",
                senderDetails,
                receiverDetails,
                "2024-06-01",
                "2024-06-02",
                ModeOfArrivalEnum.EMAIL,
                "Test Subject",
                "Test Content",
                PriorityEnum.HIGH,
                StatusEnum.NEW,
                divisionDto,
                userDto,
                true,
                2L,
                List.of(attachmentDto),
                List.of(eventDto),
                "2024-06-01T10:00:00",
                "2024-06-01T12:00:00"
        );
        Page<LetterDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        Mockito.when(letterService.getAccessibleLetters(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/letters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(authorities = "letter:read:all")
    void testGetLetterById() throws Exception {
        DivisionDto divisionDto = new DivisionDto(2, "DivisionB", "DivB");
        UserDto userDto = new UserDto(
                2L,
                "user2",
                "user2@email.com",
                "User Two",
                "9876543210",
                "ROLE_USER",
                "DivisionB",
                false
        );
        AttachmentDto attachmentDto = new AttachmentDto(2, "file2.txt", "path2", "type2", Instant.now());
        LetterEventDto eventDto = new LetterEventDto(2, userDto, EventTypeEnum.CHANGE_STATUS, Map.of("key2", "value2"), Instant.now());

        SenderDetailsDto senderDetails = new SenderDetailsDto(
                "Sender2 Name",
                "Sender2 Address",
                "sender2@email.com",
                "1231231234"
        );
        ReceiverDetailsDto receiverDetails = new ReceiverDetailsDto(
                "Receiver2 Name",
                "Receiver2 Designation",
                "Receiver2 Division"
        );

        LetterDto dto = new LetterDto(
                2,
                "REF456",
                senderDetails,
                receiverDetails,
                "2024-06-03",
                "2024-06-04",
                ModeOfArrivalEnum.EMAIL,
                "Another Subject",
                "Another Content",
                PriorityEnum.HIGH,
                StatusEnum.NEW,
                divisionDto,
                userDto,
                false,
                1L,
                List.of(attachmentDto),
                List.of(eventDto),
                "2024-06-03T10:00:00",
                "2024-06-03T12:00:00"
        );
        Mockito.when(letterService.getLetterById(anyInt(), any(), any(), any()))
                .thenReturn(dto);

        mockMvc.perform(get("/letters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(authorities = "letter:create")
    void testCreateLetter() throws Exception {
        Letter letter = new Letter();
        letter.setId(3);
        Mockito.when(letterService.createLetter(any(), any())).thenReturn(letter);

        mockMvc.perform(multipart("/letters")
                        .file("attachments", "test".getBytes())
                        .param("details", "{\"subject\":\"Test\"}")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Letter created successfully"));
    }

    @Test
    @WithMockUser(authorities = "letter:update:all")
    void testUpdateLetter() throws Exception {
        Mockito.doNothing().when(letterService).updateLetter(anyInt(), any(), any(), any(), any());

        mockMvc.perform(put("/letters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Letter updated successfully"));
    }
}