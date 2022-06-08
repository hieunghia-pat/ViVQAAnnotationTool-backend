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

import javax.swing.text.html.Option;
import java.lang.reflect.Array;
import java.util.*;

@Slf4j
@Service
public class IAAService {
//    private Long subsetId;
//
//    public IAAService(){};
//
//    public IAAService(Long subsetId) {
//        this.subsetId = subsetId;
//    }

    @Autowired
    AnnotationRepository annotationRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    UserSubsetRepository userSubsetRepository;

    public Object calIAA(Long subsetId){
        List<Image> imageList = imageRepository.findBySubsetId(subsetId);
        List<UserSubset> userSubsetsList= userSubsetRepository.findBySubsetId(subsetId);

        List<Long> imageIdList = new ArrayList<>();
        Set<UUID> userIdSet = new HashSet<>();
        Map<Long, Map<UUID, Optional<Annotation>>> imageAnnotation = new HashMap<>();

        int nQA = 0;

        //Get image ID list
        for(Image image:imageList){
            imageIdList.add(image.getId());
        }
        //Get user ID set
        for(UserSubset userSubset:userSubsetsList){
            userIdSet.add(userSubset.getUser().getId());
        }

        for(Long imageId:imageIdList){
            for(UUID userId:userIdSet){
                Optional<Annotation> annotationOptional = annotationRepository.findByUserForImage(userId, imageId);
                Map<UUID, Optional<Annotation>> userAnnotation = new HashMap<>();
                userAnnotation.put(userId, annotationOptional);
                imageAnnotation.put(imageId, userAnnotation);
            }
        }

        //Count number of annotation
//        for(Image image:imageList){
//            if(!image.isToDelete()){
//                nQA++;
//            }
//        }
//
//        ArrayList<ArrayList<Integer>> answerTypes = new ArrayList<>(3);
//        for(int i = 0; i < nQA; i++){
//            answerTypes.add(new ArrayList<>());
//        }
//
//        int i = 0;
//        //Create table Answer Type
//        for(Long imageId:imageIdList){
//            int wCount = 0, pCount = 0, sCount = 0;
//            Optional<Image> image = imageRepository.findById(imageId);
//            if(!image.get().isToDelete()){
//                for(UUID userId:userIdSet) {
//                    Annotation annotation = imageAnnotation.get(imageId).get(userId).get();
//                    Integer answerType = annotation.getAnswerType();
//                    if(answerType == 0){
//                        wCount++;
//                    }
//                    if(answerType == 1){
//                        pCount++;
//                    }
//                    if(answerType == 2){
//                        sCount++;
//                    }
//                }
//                answerTypes.get(i).add(wCount);
//                answerTypes.get(i).add(pCount);
//                answerTypes.get(i).add(sCount);
//            }
//            i++;
//        }
//
//        Map<Integer, ArrayList<ArrayList<Integer>>> test = new HashMap<>();
//        test.put(1, answerTypes);

        return imageAnnotation;
    }
}
