package uit.spring.annotation.interfaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;
import uit.spring.annotation.databases.Subset;
import uit.spring.annotation.databases.UserSubset;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = { "userId", "subsetId" })
@Component
public class UserSubsetInterface {
    private UUID id;
    private UUID userId;
    private Long subsetId;
    private boolean assigned;
    private String assignedDate;
    private String finishDate;
    private boolean isValidation;

    public UserSubsetInterface() {
    }

    public UserSubsetInterface(UUID id, UUID userId, Long subsetId, boolean assigned, String assignedDate, String finishDate, boolean isValidation) {
        this.id = id;
        this.userId = userId;
        this.subsetId = subsetId;
        this.assigned = assigned;
        this.assignedDate = assignedDate;
        this.finishDate = finishDate;
        this.isValidation = isValidation;
    }

    public UserSubsetInterface(UserSubset userSubset) {
        this.id = userSubset.getId();
        this.userId = userSubset.getUser().getId();
        this.subsetId = userSubset.getSubset().getId();
        this.assigned = true;
        this.isValidation = userSubset.isValidation();
        this.assignedDate = userSubset.getAssignDate();
        this.finishDate = userSubset.getFinishDate();
    }

    public UserSubsetInterface(Subset subset) {
        this.id = new UUID(0, 0);
        this.subsetId = subset.getId();
        this.userId = new UUID(0, 0);
        this.assigned = false;
        this.isValidation = false;
        this.assignedDate = "";
        this.finishDate = "";
    }
}
