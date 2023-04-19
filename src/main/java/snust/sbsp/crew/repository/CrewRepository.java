package snust.sbsp.crew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;

import java.util.Optional;

public interface CrewRepository extends JpaRepository<Crew, Long>, JpaSpecificationExecutor<Crew> {
  
  Optional<Crew> findByEmail(String email);

  Optional<Crew> findByIdAndRole(Long id, Role role);
}
