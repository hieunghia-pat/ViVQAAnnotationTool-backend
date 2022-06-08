package uit.spring.annotation.interfaces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;
import uit.spring.annotation.databases.Annotation;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@Component
public class AnnotationInterface {

    private UUID id = new UUID(0, 0);
    private Long imageId;
    private UUID userId;
    private String question = "";
    private String answer = "";
    private Integer questionType = 0;
    private Integer answerType = 2;
    private boolean textQA = false;
    private boolean stateQA = false;
    private boolean actionQA = false;

    public AnnotationInterface() {
    }

    public AnnotationInterface(UUID id, Long imageId, UUID userId, String question, String answer,
                               Integer questionType, Integer answerType, boolean textQA, boolean stateQA, boolean actionQA) {
        this.id = id;
        this.imageId = imageId;
        this.question = question;
        this.answer = answer;
        this.questionType = questionType;
        this.answerType = answerType;
        this.textQA = textQA;
        this.stateQA = stateQA;
        this.actionQA = actionQA;
    }

    public AnnotationInterface(Long imageId, UUID userId) {
        this.imageId = imageId;
        this.userId = userId;
    }

    public AnnotationInterface(Annotation annotation) {
        this.id = annotation.getId();
        this.imageId = annotation.getImage().getId();
        this.userId = annotation.getAnnotator().getId();
        this.question = annotation.getQuestion();
        this.answer = annotation.getAnswer();
        this.questionType = annotation.getQuestionType();
        this.answerType = annotation.getAnswerType();
        this.textQA = annotation.isTextQA();
        this.stateQA = annotation.isStateQA();
        this.actionQA = annotation.isActionQA();
    }
}
