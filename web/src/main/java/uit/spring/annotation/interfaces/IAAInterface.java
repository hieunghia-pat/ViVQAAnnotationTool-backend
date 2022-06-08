package uit.spring.annotation.interfaces;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IAAInterface {
    private ArrayList<ArrayList<Integer>> Test;

    public IAAInterface(ArrayList<ArrayList<Integer>> test) {
        Test = test;
    }
}
