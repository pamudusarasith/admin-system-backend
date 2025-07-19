package lk.gov.mohe.adminsystem.letter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lk.gov.mohe.adminsystem.division.Division;
import lk.gov.mohe.adminsystem.user.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "letters")
public class Letter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "reference", nullable = false, length = 50)
    private String reference;

    @Column(name = "sender_details")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> senderDetails;

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "mode_of_arrival", nullable = false)
    private ModeOfArrivalEnum modeOfArrival;

    @Size(max = 255)
    @NotNull
    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @ColumnDefault("'NORMAL'")
    @Generated
    @Column(name = "priority", nullable = false)
    private PriorityEnum priority;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private StatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_division_id")
    private Division assignedDivision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ColumnDefault("false")
    @Generated
    @Column(name = "is_accepted_by_user")
    private Boolean isAcceptedByUser;
}