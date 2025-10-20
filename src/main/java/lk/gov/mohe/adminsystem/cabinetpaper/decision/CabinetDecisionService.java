package lk.gov.mohe.adminsystem.cabinetpaper.decision;

import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaper;
import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperRepository;
import lk.gov.mohe.adminsystem.security.CurrentUserProvider;
import lk.gov.mohe.adminsystem.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CabinetDecisionService {
  private static final String DECISION_NOT_FOUND = "Cabinet decision not found with id: ";
  private static final String PAPER_NOT_FOUND = "Cabinet paper not found with id: ";

  private final CabinetDecisionRepository decisionRepository;
  private final CabinetPaperRepository cabinetPaperRepository;
  private final CabinetDecisionMapper decisionMapper;
  private final CurrentUserProvider currentUserProvider;

  @Transactional(readOnly = true)
  public Page<CabinetDecisionDto> getAllDecisions(Integer page, Integer pageSize) {
    Pageable pageable = PageRequest.of(page, pageSize);
    Page<CabinetDecision> decisions = decisionRepository.findAll(pageable);
    return decisions.map(decisionMapper::toDto);
  }

  @Transactional(readOnly = true)
  public CabinetDecisionDto getDecisionById(Integer id) {
    CabinetDecision decision =
        decisionRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, DECISION_NOT_FOUND + id));
    return decisionMapper.toDto(decision);
  }

  @Transactional(readOnly = true)
  public CabinetDecisionDto getDecisionByPaperId(Integer paperId) {
    CabinetDecision decision =
        decisionRepository
            .findByPaperId(paperId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cabinet decision not found for paper id: " + paperId));
    return decisionMapper.toDto(decision);
  }

  @Transactional
  public CabinetDecision createDecision(CreateCabinetDecisionRequestDto request) {
    // Check if cabinet paper exists
    CabinetPaper paper =
        cabinetPaperRepository
            .findById(request.paperId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, PAPER_NOT_FOUND + request.paperId()));

    // Check if decision already exists for this paper
    if (decisionRepository.existsByPaperId(request.paperId())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "A decision already exists for cabinet paper id: " + request.paperId());
    }

    CabinetDecision decision = decisionMapper.toEntity(request);
    decision.setPaper(paper);

    User currentUser = currentUserProvider.getCurrentUserOrThrow();
    decision.setRecordedByUser(currentUser);

    return decisionRepository.save(decision);
  }

  @Transactional
  public CabinetDecision updateDecision(Integer id, UpdateCabinetDecisionRequestDto request) {
    CabinetDecision decision =
        decisionRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, DECISION_NOT_FOUND + id));

    decisionMapper.updateEntityFromDto(request, decision);
    return decisionRepository.save(decision);
  }

  @Transactional
  public void deleteDecision(Integer id) {
    if (!decisionRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, DECISION_NOT_FOUND + id);
    }
    decisionRepository.deleteById(id);
  }
}
