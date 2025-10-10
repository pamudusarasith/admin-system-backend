package lk.gov.mohe.adminsystem.division;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DivisionService {
  private final DivisionRepository divisionRepository;
  private final DivisionMapper divisionMapper;

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
}
