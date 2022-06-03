package uit.spring.annotation.interfaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;
import uit.spring.annotation.databases.Annotation;
import uit.spring.annotation.databases.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@Component
public class ImageInterface {

    private Long id;
    private String url;
    private String filename;
    private Long subsetId;
    private boolean toDelete;
    private List<UUID> annotationIds = new ArrayList<>();

    public ImageInterface() {
    }

    public ImageInterface(Long id, String url, String filename, Long subsetId, boolean toDelete, List<UUID> annotationIds) {
        this.id = id;
        this.url = url;
        this.filename = filename;
        this.subsetId = subsetId;
        this.toDelete = toDelete;
        this.annotationIds = annotationIds;
    }

    public ImageInterface(Image image) {
        this.id = image.getId();
        this.url = image.getUrl();
        this.filename = image.getFilename();
        this.subsetId = image.getSubset().getId();
        this.toDelete = image.isToDelete();
        for (Annotation annotation: image.getAnnotations()) {
            this.annotationIds.add(annotation.getId());
        }
    }
}
