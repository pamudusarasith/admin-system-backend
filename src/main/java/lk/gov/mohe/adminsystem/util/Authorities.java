package lk.gov.mohe.adminsystem.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class Authorities {
  public static final GrantedAuthority letterReadAll =
      new SimpleGrantedAuthority("letter:read:all");
  public static final GrantedAuthority letterReadUnassigned =
      new SimpleGrantedAuthority("letter:read:unassigned");
  public static final GrantedAuthority letterReadDivision =
      new SimpleGrantedAuthority("letter:read:division");
  public static final GrantedAuthority letterReadOwn =
      new SimpleGrantedAuthority("letter:read:own");
}
