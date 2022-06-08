package uit.spring.annotation.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uit.spring.annotation.databases.Annotation;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, UUID> {
    List<Annotation> findByImageId(Long image_id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(
            value = "UPDATE annotation SET question=?2, answer=?3, question_type=?4, answer_type=?5, text_qa=?6, state_qa=?7, action_qa=?8, image_id=?9, user_id=?10 " +
                    "WHERE id=?1",
            nativeQuery = true
    )
    void updateById(UUID id, String question, String answer, Integer questionType, Integer answerType,
                boolean textQA, boolean stateQA, boolean actionQA, Long imageId, UUID userId);

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM annotation WHERE id=?1",
            nativeQuery = true
    )
    void deleteById(UUID id);

    @Query(
            value = "SELECT * FROM annotation WHERE user_id=?1 AND image_id=?2",
            nativeQuery = true
    )
    Optional<Annotation> findByUserForImage(UUID userId, Long imageId);

    @Transactional
    @Modifying
    @Query(
            value = "INSERT INTO annotation (question, answer, question_type, answer_type, text_QA, state_QA, action_QA, image_id, user_id) " +
                    "VALUES (?3, ?4, ?5, ?6, ?7, ?8, ?9, ?2, ?1)",
            nativeQuery = true
    )
    void insert(UUID userId, Long imageId, String question, String answer, Integer questionType, Integer answerType, boolean textQA, boolean stateQA, boolean actionQA);
}
