package uit.spring.annotation.interfaces;

import lombok.Data;

@Data
public class ProgressInterface {
    private Integer totalImages;
    private Integer totalAnnotatedImages;
    private Integer totalTextQA;
    private Integer totalStateQA;
    private Integer totalActionQA;

    public ProgressInterface(Integer totalImages, Integer totalAnnotatedImages, Integer totalTextQA, Integer totalStateQA, Integer totalActionQA) {
        this.totalImages = totalImages;
        this.totalAnnotatedImages = totalAnnotatedImages;
        this.totalTextQA = totalTextQA;
        this.totalStateQA = totalStateQA;
        this.totalActionQA = totalActionQA;
    }
}
