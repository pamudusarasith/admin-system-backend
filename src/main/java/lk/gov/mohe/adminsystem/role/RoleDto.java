package lk.gov.mohe.adminsystem.role;

import java.util.List;
import lombok.Data;

@Data
public class RoleDto {
  private Integer id;
  private String name;
  private String description;
  private List<String> permissions;
  private long userCount;
}
