package lk.gov.mohe.adminsystem.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PermissionCategoryDto {
  private Integer id;
  private String name;
  private List<PermissionDto> permissions;
  private List<PermissionCategoryDto> subCategories;
}
