package uit.spring.annotation.interfaces;

import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class SubsetStatisticsInterface {
    private UUID userId;
    private Long subsetId;
    private Integer totalImages;
    private Integer totalTextQA;
    private Integer totalStateQA;
    private Integer totalActionQA;
    private Integer totalAnnotatedImages;
    private Integer totalDeletedImages;
    private List<Integer> questionLengths;
    private List<Integer> answerLengths;
    private Set<String> objects;
    private Set<String> verbs;

    public SubsetStatisticsInterface(UUID userId, Long subsetId, Integer totalImages, Integer totalTextQA,
                                     Integer totalStateQA, Integer totalActionQA, Integer totalAnnotatedImages,
                                     Integer totalDeletedImages, List<Integer> questionLengths, List<Integer> answerLengths,
                                     Set<String> objects, Set<String> verbs) {
        this.userId = userId;
        this.subsetId = subsetId;
        this.totalImages = totalImages;
        this.totalTextQA = totalTextQA;
        this.totalStateQA = totalStateQA;
        this.totalActionQA = totalActionQA;
        this.totalAnnotatedImages = totalAnnotatedImages;
        this.totalDeletedImages = totalDeletedImages;
        this.questionLengths = questionLengths;
        this.answerLengths = answerLengths;
        this.objects = objects;
        this.verbs = verbs;
    }
}
