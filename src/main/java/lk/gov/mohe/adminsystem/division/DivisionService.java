package lk.gov.mohe.adminsystem.division;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DivisionService {
  private final DivisionRepository divisionRepository;
  private final DivisionMapper divisionMapper;

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
    if (divisionRepository.existsByName(dto.name())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Division with the same name already exists");
    }
    Division division = divisionMapper.createOrUpdateRequestDtoToDivision(dto);
    divisionRepository.save(division);
  }
}
