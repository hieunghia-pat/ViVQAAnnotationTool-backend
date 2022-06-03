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
    private Date assignDate;
    @Column(name = "finish_date", nullable = false)
    private Date finishDate;
    @Column(name = "is_validation", nullable = false)
    boolean isValidation;

    public UserSubset() {
    }

    public UserSubset(User user, Subset subset, String assignDate, String finishDate, boolean isValidation) {
        this.user = user;
        this.subset = subset;
        setAssignDate(assignDate);
        setFinishDate(finishDate);
        this.isValidation = isValidation;
    }

    public String getAssignDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(this.assignDate);
    }

    public String getFinshDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(this.finishDate);
    }

    public void setAssignDate(String assignDate) {
        try {
            this.assignDate = new SimpleDateFormat("dd/MM/yyyy").parse(assignDate);
        }
        catch(ParseException exception) {
            log.info(exception.getMessage());
        }
    }

    public void setFinishDate(String finishDate) {
        try {
            this.finishDate = new SimpleDateFormat("dd/MM/yyyy").parse(finishDate);
        }
        catch(ParseException exception) {
            log.info(exception.getMessage());
        }
    }
}
