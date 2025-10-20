package lk.gov.mohe.adminsystem.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSearchParams {
  private String query = "";
  private String roleName;
  private String divisionName;
  private Integer divisionId;
  private Boolean assignableOnly;
  
  private Integer page = 0;
  private Integer pageSize = 10;
}
