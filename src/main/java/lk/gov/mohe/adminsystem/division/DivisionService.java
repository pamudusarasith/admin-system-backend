package lk.gov.mohe.adminsystem.division;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lk.gov.mohe.adminsystem.notification.NotificationDto;
import lk.gov.mohe.adminsystem.notification.NotificationService;
import lk.gov.mohe.adminsystem.user.User;
import lk.gov.mohe.adminsystem.user.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DivisionService {
  private final DivisionRepository divisionRepository;
  private final DivisionMapper divisionMapper;

  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Transactional(readOnly = true)
  public Page<DivisionDto> getDivisions(String query, Integer page, Integer pageSize) {
    Pageable pageable = Pageable.ofSize(pageSize).withPage(page);

    Specification<Division> spec = null;

    if (query != null && !query.isEmpty()) {
      String likeQuery = "%" + query.toLowerCase() + "%";
      spec =
          (root, criteriaQuery, criteriaBuilder) ->
              criteriaBuilder.or(
                  criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeQuery),
                  criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeQuery));
    }

    return divisionRepository.findAll(spec, pageable).map(divisionMapper::toDto);
  }

  @Transactional
  public void createDivision(CreateOrUpdateDivisionRequestDto dto) {
    if (divisionRepository.existsByNameIgnoreCase(dto.name())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Division with the same name already exists");
    }
    Division division = divisionMapper.dtoToDivision(dto);
    divisionRepository.save(division);
  }

  @Transactional
  public void updateDivision(Integer id, CreateOrUpdateDivisionRequestDto dto) {
    Division existingDivision =
        divisionRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));

    if (!existingDivision.getName().equalsIgnoreCase(dto.name())
        && divisionRepository.existsByNameIgnoreCase(dto.name())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Division with the same name already exists");
    }

    divisionMapper.updateDivisionFromDto(dto, existingDivision);
    divisionRepository.save(existingDivision);

    List<User> usersInDivision = userRepository.findByDivisionId(existingDivision.getId());

    if (!usersInDivision.isEmpty()) {
      NotificationDto notification =
              new NotificationDto(
                      "Division Details Updated",
                      "Details for your division, '" + existingDivision.getName() + "', have been updated.",
                      "/profile"); // Link to a relevant page

      for (User user : usersInDivision) {
        notificationService.sendNotification(user.getId(), notification);
      }
    }
  }

  @Transactional
  public void deleteDivision(Integer id) {
    Division existingDivision =
        divisionRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Division not found"));
    divisionRepository.delete(existingDivision);
  }
}
