package lk.gov.mohe.adminsystem.cabinetpaper.category;

import lk.gov.mohe.adminsystem.cabinetpaper.CabinetPaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CabinetPaperCategoryService {
  private final CabinetPaperCategoryMapper cabinetPaperCategoryMapper;
  private final CabinetPaperCategoryRepository repository;
  private final CabinetPaperRepository cabinetPaperRepository;

  @Transactional(readOnly = true)
  public Page<CabinetPaperCategoryDto> getAllCategories(
      String query, Integer page, Integer pageSize) {
    Pageable pageable = PageRequest.of(page, pageSize);

    Page<CabinetPaperCategory> categories;
    if (StringUtils.hasText(query)) {
      categories = repository.findByNameContainingIgnoreCase(query, pageable);
    } else {
      categories = repository.findAll(pageable);
    }
    return categories.map(cabinetPaperCategoryMapper::toDto);
  }

  @Transactional
  public CabinetPaperCategory createCategory(CreateCabinetPaperCategoryRequestDto request) {
    if (repository.existsByName(request.name())) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Category with name '" + request.name() + "' already exists");
    }

    CabinetPaperCategory category = cabinetPaperCategoryMapper.toEntity(request);
    return repository.save(category);
  }

  @Transactional
  public CabinetPaperCategory updateCategory(
      Integer id, UpdateCabinetPaperCategoryRequestDto request) {
    CabinetPaperCategory category =
        repository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + id));

    if (repository.existsByNameAndIdNot(request.name(), id)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Category with name '" + request.name() + "' already exists");
    }

    cabinetPaperCategoryMapper.updateEntityFromDto(request, category);
    return repository.save(category);
  }

  @Transactional
  public void deleteCategory(Integer id) {
    CabinetPaperCategory category =
        repository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + id));

    if (cabinetPaperRepository.existsByCategory(category)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Cannot delete category. It is being used by one or more cabinet papers");
    }

    repository.deleteById(id);
  }
}
