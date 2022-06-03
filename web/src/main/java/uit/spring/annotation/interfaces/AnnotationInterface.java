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

    private UUID id;
    private Long imageId;
    private String question;
    private String answer;
    private Integer questionType;
    private Integer answerType;
    private boolean textQA;
    private boolean stateQA;
    private boolean actionQA;

    public AnnotationInterface() {
    }

    public AnnotationInterface(UUID id, Long imageId, String question, String answer, Integer questionType, Integer answerType, boolean textQA, boolean stateQA, boolean actionQA) {
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

    public AnnotationInterface(Annotation annotation) {
        this.id = annotation.getId();
        this.imageId = annotation.getImage().getId();
        this.question = annotation.getQuestion();
        this.answer = annotation.getAnswer();
        this.questionType = annotation.getQuestionType();
        this.answerType = annotation.getAnswerType();
        this.textQA = annotation.isTextQA();
        this.stateQA = annotation.isStateQA();
        this.actionQA = annotation.isActionQA();
    }
}
