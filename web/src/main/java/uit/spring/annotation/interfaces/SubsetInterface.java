package uit.spring.annotation.interfaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.databases.Subset;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@EqualsAndHashCode(of = "id")
@Component
public class SubsetInterface {

    private Long id;
    private List<Long> imageIds = new ArrayList<>();

    public SubsetInterface() {
    }

    public SubsetInterface(Long id, List<Long> imageIds) {
        this.id = id;
        this.imageIds = imageIds;
    }

    public SubsetInterface(Subset subset) {
        this.id = subset.getId();

        for (Image image: subset.getImages()) {
            imageIds.add(image.getId());
        }
    }
}
