package uit.spring.annotation.services;

import lombok.Data;
import org.springframework.stereotype.Service;
import uit.spring.annotation.interfaces.ReturnInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Data
public class AssignmentDateChecking {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ReturnInterface checkTime(String strAssignedDate, String strFinishDate) {
        Date assignedDate;
        Date finishDate;
        try {
            assignedDate = simpleDateFormat.parse(strAssignedDate);
            finishDate = simpleDateFormat.parse(strFinishDate);
        }
        catch (ParseException parseException) {
            return new ReturnInterface(
                    -1,
                    parseException.getMessage()
            );
        }

        Long duration = finishDate.getTime() - assignedDate.getTime();
        if (duration < 0) {
            return new ReturnInterface(
                    -1,
                    "Time is not valid. Assigned date must be before the finish date"
            );
        }

        return new ReturnInterface(
                0,
                "Nothing to concern"
        );
    }
}
