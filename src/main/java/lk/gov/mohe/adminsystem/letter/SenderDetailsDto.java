package lk.gov.mohe.adminsystem.letter;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

public record SenderDetailsDto(
    @NotBlank
    String name,

    String address,

    String email,

    String phoneNumber
) {
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("address", address);
        map.put("email", email);
        map.put("phone_number", phoneNumber);
        return map;
    }
}
