package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LetterControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LetterService letterService;

    @InjectMocks
    private LetterController letterController;

    private CreateOrUpdateLetterRequestDto createDto;
    private AssignDivisionRequestDto assignDivisionDto;

    /**
     * Helper method to create a mock Authentication object with a JWT principal.
     */
    private Authentication createMockAuthentication(int userId, int divisionId, String... authorities) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", userId)
                .claim("divisionId", divisionId)
                .build();

        Authentication auth = mock(Authentication.class);

        // Stub the getPrincipal() method
        when(auth.getPrincipal()).thenReturn(jwt);

        // Corrected: Use the doReturn(...).when(...) pattern for methods with complex generics
        Collection<GrantedAuthority> grantedAuthorities = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        doReturn(grantedAuthorities).when(auth).getAuthorities();

        return auth;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(letterController).build();

        SenderDetailsDto senderDetails = new SenderDetailsDto("Sender", "Address", "email@test.com", "123");
        createDto = new CreateOrUpdateLetterRequestDto("REF123", senderDetails, null, "2025-01-01", "2025-01-02", ModeOfArrivalEnum.EMAIL, "Subject", "Content", PriorityEnum.NORMAL);
        assignDivisionDto = new AssignDivisionRequestDto(10);
    }

    @Test
    void getLetters_ShouldPassClaimsAndAuthoritiesToService() throws Exception {
        Authentication mockAuth = createMockAuthentication(100, 10, "letter:division:read");
        when(letterService.getAccessibleLetters(anyInt(), anyInt(), any(Collection.class), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/letters")
                        .param("page", "0")
                        .param("pageSize", "10")
                        .requestAttr("org.springframework.security.core.Authentication", mockAuth))
                .andExpect(status().isOk());

        verify(letterService).getAccessibleLetters(
                eq(100),
                eq(10),
                argThat(coll -> coll.contains("letter:division:read")),
                any(),
                eq(0),
                eq(10)
        );
    }

    @Test
    void createLetter_ShouldReturnCreated() throws Exception {
        Authentication mockAuth = createMockAuthentication(100, 10, "letter:create");
        Letter mockSavedLetter = new Letter();
        mockSavedLetter.setId(1);
        MockMultipartFile details = new MockMultipartFile("details", "", "application/json", objectMapper.writeValueAsBytes(createDto));
        when(letterService.createLetter(any(CreateOrUpdateLetterRequestDto.class), any())).thenReturn(mockSavedLetter);

        mockMvc.perform(multipart("/letters")
                        .file(details)
                        .requestAttr("org.springframework.security.core.Authentication", mockAuth))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/letters/1"));
    }

    @Test
    void assignDivision_ShouldSucceed() throws Exception {
        Authentication mockAuth = createMockAuthentication(100, 10, "letter:assign:division");
        doNothing().when(letterService).assignDivision(anyInt(), anyInt());

        mockMvc.perform(put("/letters/1/division")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDivisionDto))
                        .requestAttr("org.springframework.security.core.Authentication", mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Division assigned successfully"));
    }

    @Test
    void acceptLetter_ShouldPassUserIdToService() throws Exception {
        Authentication mockAuth = createMockAuthentication(100, 10);
        doNothing().when(letterService).acceptLetter(anyInt(), anyInt());

        mockMvc.perform(patch("/letters/1/user")
                        .param("action", "accept")
                        .requestAttr("org.springframework.security.core.Authentication", mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Letter accepted successfully"));

        verify(letterService).acceptLetter(1, 100);
    }
}