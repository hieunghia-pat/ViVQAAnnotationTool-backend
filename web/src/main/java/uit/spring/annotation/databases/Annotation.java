package uit.spring.annotation.databases;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Data
@Entity(name = "Annotation")
@Table(name = "Annotation")
@EqualsAndHashCode(of = "id")
public class Annotation {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "image_id")
    Long imageId;
    @Column(name = "question")
    private String question;
    @Column(name = "answer")
    private String answer;
    @Column(name = "question_type")
    private Integer questionType;
    @Column(name = "answer_type")
    private Integer answerType;
    @Column(name = "text_QA")
    private boolean textQA;
    @Column(name = "state_QA")
    private boolean stateQA;
    @Column(name = "action_QA", nullable = false)
    private boolean actionQA;
    @ManyToOne
    @JoinColumn(name = "image_id", nullable = false, insertable = false, updatable = false)
    private Image image;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User annotator;

    public Annotation() {

    }

    public Annotation(String question, String answer, Integer questionType,
                      Integer answerType, boolean textQA, boolean stateQA, boolean actionQA, Image image, User annotator) {
        this.question = question;
        this.answer = answer;
        this.questionType = questionType;
        this.answerType = answerType;
        this.textQA = textQA;
        this.stateQA = stateQA;
        this.actionQA = actionQA;
        this.imageId = image.getId();
        this.userId = annotator.getId();
    }

    public Annotation(UUID userId, Long imageId, String question, String answer,
                      Integer questionType, Integer answerType, boolean textQA, boolean stateQA, boolean actionQA) {
        this.userId = userId;
        this.imageId = imageId;
        this.question = question;
        this.answer = answer;
        this.questionType = questionType;
        this.answerType = answerType;
        this.textQA = textQA;
        this.stateQA = stateQA;
        this.actionQA = actionQA;
    }
}
