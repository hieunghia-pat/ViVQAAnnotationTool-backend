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
    private Long subsetId;

    public IAAService(){};

    public IAAService(Long subsetId) {
        this.subsetId = subsetId;
    }

    @Autowired
    AnnotationRepository annotationRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    UserSubsetRepository userSubsetRepository;

    public Object calIAA(){
        List<Image> imageList = imageRepository.findBySubsetId(subsetId);
        List<UserSubset> userSubsetsList= userSubsetRepository.findBySubsetId(subsetId);

        List<Long> imageIdList = new ArrayList<>();
        Set<UUID> userIdSet = new HashSet<>();
        Map<Long, Object> imageAnnotation = new HashMap<>();

        for(Image image:imageList){
            imageIdList.add(image.getId());
        }
        for(UserSubset userSubset:userSubsetsList){
            userIdSet.add(userSubset.getUser().getId());
        }

        for(Long imageId:imageIdList){
            for(UUID userId:userIdSet){
                Optional<Annotation> annotationOptional = annotationRepository.findByUserForImage(userId, imageId);
                Map<UUID, Object> userAnnotation = new HashMap<>();
                userAnnotation.put(userId, annotationOptional);
                imageAnnotation.put(imageId, userAnnotation);
            }
        }

        return imageAnnotation;
    }
}
