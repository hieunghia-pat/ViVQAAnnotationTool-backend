package uit.spring.annotation.interfaces;

import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class PosInterface {
    private UUID userId;
    private Long subsetId;
    private Map<String, Integer> objects;
    private Map<String, Integer> verbs;

    public PosInterface(UUID userId, Long subsetId, Map<String, Integer> objects, Map<String, Integer> verbs) {
        this.userId = userId;
        this.subsetId = subsetId;
        this.objects = objects;
        this.verbs = verbs;
    }
}
