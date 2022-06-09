package uit.spring.annotation.interfaces;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IAAInterface {
    private Object Test;

    public IAAInterface(Object test) {
        Test = test;
    }
}
