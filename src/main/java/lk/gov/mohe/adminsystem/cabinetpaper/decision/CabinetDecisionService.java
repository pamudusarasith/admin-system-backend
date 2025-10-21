package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaper;
import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CabinetDecisionService {
  private static final String DECISION_NOT_FOUND = "Cabinet decision not found for paper id: ";
  private static final String PAPER_NOT_FOUND = "Cabinet paper not found with id: ";

  private final CabinetDecisionRepository decisionRepository;
  private final CabinetPaperRepository cabinetPaperRepository;
  private final CabinetDecisionMapper decisionMapper;
  private final CurrentUserProvider currentUserProvider;

  @Transactional(readOnly = true)
  public CabinetDecisionDto getDecisionByPaperId(Integer paperId) {
    CabinetDecision decision =
        decisionRepository
            .findByPaperId(paperId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        DECISION_NOT_FOUND + paperId));
    return decisionMapper.toDto(decision);
  }

  @Transactional
  public void createDecision(Integer paperId, CreateCabinetDecisionRequestDto request) {
    // Validate paperId
    if (paperId == null || paperId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paper ID");
    }

    // Check if cabinet paper exists
    CabinetPaper paper =
        cabinetPaperRepository
            .findById(paperId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, PAPER_NOT_FOUND + paperId));

    // Check if decision already exists for this paper
    if (decisionRepository.existsByPaperId(paperId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "A decision already exists for cabinet paper id: " + paperId);
    }

    CabinetDecision decision = decisionMapper.toEntity(request);
    decision.setPaper(paper);

    User currentUser = currentUserProvider.getCurrentUserOrThrow();
    decision.setRecordedByUser(currentUser);

    decisionRepository.save(decision);
  }

  @Transactional
  public void updateDecision(Integer paperId, UpdateCabinetDecisionRequestDto request) {
    // Validate paperId
    if (paperId == null || paperId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paper ID");
    }

    CabinetDecision decision =
        decisionRepository
            .findByPaperId(paperId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, DECISION_NOT_FOUND + paperId));

    decisionMapper.updateEntityFromDto(request, decision);
    decisionRepository.save(decision);
  }

  @Transactional
  public void deleteDecision(Integer paperId) {
    // Validate paperId
    if (paperId == null || paperId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paper ID");
    }

    CabinetDecision decision =
        decisionRepository
            .findByPaperId(paperId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, DECISION_NOT_FOUND + paperId));

    decisionRepository.delete(decision);
  }
}
