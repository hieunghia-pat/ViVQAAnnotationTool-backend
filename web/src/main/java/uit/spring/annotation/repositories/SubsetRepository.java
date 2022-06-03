package uit.spring.annotation.repositories;

import uit.spring.annotation.databases.Subset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubsetRepository extends JpaRepository<Subset, Long> {
}

