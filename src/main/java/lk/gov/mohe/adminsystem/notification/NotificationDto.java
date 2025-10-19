package lk.gov.mohe.adminsystem.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String title;
    private String message;
    private String link; // Optional: A link to the relevant page (e.g., /letters/123)
}