package uit.spring.annotation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uit.spring.annotation.databases.Annotation;
import uit.spring.annotation.databases.Image;
import uit.spring.annotation.databases.UserSubset;
import uit.spring.annotation.interfaces.IAAInterface;
import uit.spring.annotation.repositories.AnnotationRepository;
import uit.spring.annotation.repositories.ImageRepository;
import uit.spring.annotation.repositories.UserSubsetRepository;

import java.util.*;

@Slf4j
@Service
public class IAAService {

    @Autowired
    AnnotationRepository annotationRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    UserSubsetRepository userSubsetRepository;

    public IAAInterface calIAA(Long subsetId){
        List<Image> imageList = imageRepository.findBySubsetId(subsetId);
        List<UserSubset> userSubsetsList= userSubsetRepository.findBySubsetId(subsetId);

        Map<Long, Map<UUID, Map<String, Long>>> imageAnnotation = new HashMap<>();

        int nQA = 0;

        //Get image ID list
        List<Long> imageIdList = new ArrayList<>();
        for(Image image:imageList){
            imageIdList.add(image.getId());
        }

        //Get user ID set
        Set<UUID> userIdSet = new HashSet<>();
        for(UserSubset userSubset:userSubsetsList){
            userIdSet.add(userSubset.getUser().getId());
        }

        //Create image annotation type
        for(Long imageId:imageIdList){
            Map<UUID, Map<String, Long>> userAnnotation = new HashMap<>();
            for(UUID userId:userIdSet){
                Optional<Annotation> annotationOptional = annotationRepository.findByUserForImage(userId, imageId);
                Map<String, Long> annotationType = new HashMap<>();
                if(annotationOptional.isPresent()){
                    Annotation annotation = annotationOptional.get();
                    annotationType.put("answerType", Long.valueOf(annotation.getAnswerType()));
                    annotationType.put("questionType", Long.valueOf(annotation.getQuestionType()));
                    annotationType.put("stateQA", (long) (annotation.isStateQA() ? 1 : 0));
                    annotationType.put("textQA", (long) (annotation.isTextQA() ? 1 : 0));
                    annotationType.put("actionQA", (long) (annotation.isActionQA() ? 1 : 0));
                }
                userAnnotation.put(userId, annotationType);
            }
            imageAnnotation.put(imageId, userAnnotation);
        }

        //Count number of annotation
        for(Image image:imageList){
            if(!image.isToDelete()){
                nQA++;
            }
        }
        ArrayList<ArrayList<Long>> answerTypes = createTable("answerType", nQA, 3,
                imageIdList, userIdSet, imageAnnotation);
        ArrayList<ArrayList<Long>> questionTypes = createTable("questionType", nQA, 6,
                imageIdList, userIdSet, imageAnnotation);
        ArrayList<ArrayList<Long>> stateQAs = createTable("stateQA", nQA, 2,
                imageIdList, userIdSet, imageAnnotation);
        ArrayList<ArrayList<Long>> textQAs = createTable("textQA", nQA, 2,
                imageIdList, userIdSet, imageAnnotation);
        ArrayList<ArrayList<Long>> actionQAs = createTable("actionQA", nQA, 2,
                imageIdList, userIdSet, imageAnnotation);

        IAAInterface IAAScores = new IAAInterface(calculate(answerTypes),
                                    calculate(questionTypes),
                                    calculate(stateQAs),
                                    calculate(textQAs),
                                    calculate(actionQAs));

        return IAAScores;
    }

    public ArrayList<ArrayList<Long>> createTable(String key, Integer nQA, Integer numType, List<Long> imageIdList,
                                                  Set<UUID> userIdSet, Map<Long, Map<UUID, Map<String, Long>>> imageAnnotation){

        Map<Long, Long> typeCount = new HashMap<>();

        ArrayList<ArrayList<Long>> typeTable = new ArrayList<>(nQA);
        for(int i = 0; i < nQA; i++){
            typeTable.add(new ArrayList<>());
        }

        int index = 0;
        for(Long imageId:imageIdList){
            for(int i = 0; i < numType; i++){
                typeCount.put((long) i, (long) 0);
            }

            Optional<Image> imageOptional = imageRepository.findById(imageId);

            if(imageOptional.isPresent()) {
                Image image = imageOptional.get();
                if (!image.isToDelete()) {
                    for (UUID userId : userIdSet) {
                        Map<String, Long> annotationType = imageAnnotation.get(imageId).get(userId);
                        Long value = annotationType.get(key);
                        typeCount.put(value, typeCount.get(value)+1);
                    }
                    for(Map.Entry<Long, Long> entry : typeCount.entrySet()){
                        typeTable.get(index).add(entry.getValue());
                    }
                    index++;
                }
            }
        }
        return typeTable;
    }

    public Map<String, Float> calculate(ArrayList<ArrayList<Long>> annArrayList){

        //Convert 2D ArrayList to 2D Array
        Long[][] annArray = annArrayList.stream().map(u -> u.toArray(new Long[0])).toArray(Long[][]::new);

        Map<String, Float> scores = new HashMap<>();
        scores.put("fleissKappa", FleissKappa.compute(annArray));
        scores.put("percentAgreement", PercentAgreement.compute(annArray));

        return scores;
    }
}
