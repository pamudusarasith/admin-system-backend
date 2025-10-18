package lk.gov.mohe.adminsystem.permission;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PermissionDto(Integer id, String name, String label, String description) {}
