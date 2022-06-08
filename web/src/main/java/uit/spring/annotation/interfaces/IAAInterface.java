package uit.spring.annotation.interfaces;

import lombok.Data;

@Data
public class IAAInterface {
    private Object Test;

    public IAAInterface(Object test) {
        Test = test;
    }
}
