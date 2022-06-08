package uit.spring.annotation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uit.spring.annotation.databases.Annotation;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.databases.UserSubset;
import uit.spring.annotation.repositories.AnnotationRepository;
import uit.spring.annotation.repositories.ImageRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;

import java.util.*;

@Slf4j
@Service
public class IAAService {

    Map<Long, Map<UUID, Map<String, Integer>>> imageAnnotation = new HashMap<>();

    List<Long> imageIdList = new ArrayList<>();

    Set<UUID> userIdSet = new HashSet<>();

    ArrayList<ArrayList<Integer>> answerTypes = new ArrayList<>();

    @Autowired
    AnnotationRepository annotationRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    UserSubsetRepository userSubsetRepository;

    public Object calIAA(Long subsetId){
        List<Image> imageList = imageRepository.findBySubsetId(subsetId);
        List<UserSubset> userSubsetsList= userSubsetRepository.findBySubsetId(subsetId);

        int nQA = 0;

        //Get image ID list
        for(Image image:imageList){
            imageIdList.add(image.getId());
        }
        //Get user ID set
        for(UserSubset userSubset:userSubsetsList){
            userIdSet.add(userSubset.getUser().getId());
        }

        //Create image annotation type
        for(Long imageId:imageIdList){
            for(UUID userId:userIdSet){
                Optional<Annotation> annotationOptional = annotationRepository.findByUserForImage(userId, imageId);
                Map<String, Integer> annotationType = new HashMap<>();
                if(annotationOptional.isPresent()){
                    Annotation annotation = annotationOptional.get();
                    annotationType.put("answerType", annotation.getAnswerType());
                    annotationType.put("questionType", annotation.getQuestionType());
                    annotationType.put("stateQA", annotation.isStateQA() ? 1 : 0);
                    annotationType.put("textQA", annotation.isTextQA() ? 1 : 0);
                    annotationType.put("actionQA", annotation.isActionQA() ? 1 : 0);
                }

                Map<UUID, Map<String, Integer>> userAnnotation = new HashMap<>();
                userAnnotation.put(userId, annotationType);
                imageAnnotation.put(imageId, userAnnotation);
            }
        }

        //Count number of annotation
        for(Image image:imageList){
            if(!image.isToDelete()){
                nQA++;
            }
        }
        answerTypes = createTable("answerType", nQA, 3);

        return answerTypes;
    }

    public ArrayList<ArrayList<Integer>> createTable(String key, Integer nQA, Integer numType){
        Map<Integer, Integer> typeCount = new HashMap<>();

        ArrayList<ArrayList<Integer>> typeTable = new ArrayList<>(nQA);
        for(int i = 0; i < nQA; i++){
            typeTable.add(new ArrayList<>());
        }

        int index = 0;
        for(Long imageId:imageIdList){
            for(int i = 0; i < numType; i++){
                typeCount.put(i, 0);
            }

            Optional<Image> imageOptional = imageRepository.findById(imageId);

            if(imageOptional.isPresent()) {
                Image image = imageOptional.get();
                if (!image.isToDelete()) {
                    for (UUID userId : userIdSet) {
                        Map<String, Integer> annotationType = imageAnnotation.get(imageId).get(userId);
                        Integer value = annotationType.get(key);
                        if(value == null){
                            value = 0;
                        }
                        typeCount.put(value, typeCount.get(value)+1);
//                        typeCount.put(0, value);
                    }
                }
            }
            for(Map.Entry<Integer, Integer> entry : typeCount.entrySet()){
//                typeTable.get((int) (long) imageId).add(entry.getValue());
                typeTable.get(index).add(entry.getValue());
            }
            index++;
        }

        return typeTable;
    }
}
