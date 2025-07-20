package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;

    public PaginatedResponse<Letter> getLetters(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Letter> lettersPage = letterRepository.findAll(pageable);

        return new PaginatedResponse<>(lettersPage);
    }

    public Letter createLetter(CreateOrUpdateLetterRequestDto request) {
        Letter letter = new Letter();
        setLetterParameters(request, letter);
        letter.setStatus(StatusEnum.NEW);

        return letterRepository.save(letter);
    }

    public void updateLetter(Integer id, CreateOrUpdateLetterRequestDto request) {
        Letter letter = letterRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Letter not found with id: "
                + id));

        setLetterParameters(request, letter);

        letterRepository.save(letter);
    }

    private void setLetterParameters(CreateOrUpdateLetterRequestDto request,
                                     Letter letter) {
        letter.setReference(request.reference());
        letter.setSenderDetails(request.senderDetails().toMap());
        letter.setSentDate(request.sentDate() != null ?
            LocalDate.parse(request.sentDate()) : null);
        letter.setReceivedDate(LocalDate.parse(request.receivedDate()));
        letter.setModeOfArrival(request.modeOfArrival());
        letter.setSubject(request.subject());
        letter.setContent(request.content());
        letter.setPriority(request.priority());
    }
}
