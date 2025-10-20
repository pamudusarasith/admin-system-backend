package lk.gov.mohe.adminsystem.division;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DivisionServiceTest {

    @Mock
    private DivisionRepository divisionRepository;

    @Mock
    private DivisionMapper divisionMapper;

    @InjectMocks
    private DivisionService divisionService;

    private Division division;
    private DivisionDto divisionDto;
    private CreateOrUpdateDivisionRequestDto createDto;

    @BeforeEach
    void setUp() {
        // Setup common objects for tests
        division = new Division();
        division.setId(1);
        division.setName("Test Division");
        division.setDescription("Test Description");

        divisionDto = new DivisionDto(1, "Test Division", "Test Description");

        createDto = new CreateOrUpdateDivisionRequestDto("New Division", "New Description");
    }

}