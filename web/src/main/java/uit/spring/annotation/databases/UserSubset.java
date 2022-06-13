package uit.spring.annotation.databases;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Data
@Entity(name = "UserSubset")
@Table(name = "users_subset")
@Slf4j
public class UserSubset {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", unique = false)
    private User user;

    @ManyToOne(targetEntity = Subset.class)
    @JoinColumn(name = "subset_id", unique = false)
    private Subset subset;
    @Column(name = "assign_date", nullable = false)
    private String assignDate;
    @Column(name = "finish_date", nullable = false)
    private String finishDate;
    @Column(name = "is_validation", nullable = false)
    boolean isValidation;

    public UserSubset() {
    }

    public UserSubset(User user, Subset subset, String assignDate, String finishDate, boolean isValidation) {
        this.user = user;
        this.subset = subset;
        this.assignDate = assignDate;
        this.finishDate = finishDate;
        this.isValidation = isValidation;
    }
}
