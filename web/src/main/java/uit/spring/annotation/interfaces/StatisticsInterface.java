package uit.spring.annotation.interfaces;

import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class StatisticsInterface {
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

    public StatisticsInterface(UUID userId, Long subsetId, Integer totalImages, Integer totalTextQA,
                               Integer totalStateQA, Integer totalActionQA, Integer totalAnnotatedImages,
                               Integer totalDeletedImages, List<Integer> questionLengths, List<Integer> answerLengths) {
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
    }
}
