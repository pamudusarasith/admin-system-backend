package lk.gov.mohe.adminsystem.letter;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReceiverDetailsDto(String name, String designation, @JsonProperty("division_name")String divisionName) {}
