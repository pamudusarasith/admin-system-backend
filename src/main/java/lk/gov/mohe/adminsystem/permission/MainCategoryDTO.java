package lk.gov.mohe.adminsystem.permission;

import java.util.List;

public record MainCategoryDTO(Integer id, String name, List<SubCategoryDTO> subCategories) {}
