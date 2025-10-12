package lk.gov.mohe.adminsystem.permission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "permissions")
public class Permission {
  @Id
  @ColumnDefault("nextval('permissions_id_seq')")
  @Column(name = "id", nullable = false)
  private Integer id;

  @Size(max = 100)
  @NotNull
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Size(max = 100)
  @NotNull
  @Column(name = "label", nullable = false, length = 100)
  private String label;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  @JoinColumn(name = "category_id")
  private PermissionCategory category;
}
