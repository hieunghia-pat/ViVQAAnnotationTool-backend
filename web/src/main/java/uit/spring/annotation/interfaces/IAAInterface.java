package uit.spring.annotation.interfaces;

import lombok.Data;

@Data
public class IAAInterface {
    private Object answerType;
    private Object questionType;
    private Object textQA;
    private Object stateQA;
    private Object actionQA;

    public IAAInterface(){};

    public IAAInterface(Object answerType,
                        Object questionType,
                        Object textQA,
                        Object stateQA,
                        Object actionQA) {
        this.answerType = answerType;
        this.questionType = questionType;
        this.textQA = textQA;
        this.stateQA = stateQA;
        this.actionQA = actionQA;
    }
}
