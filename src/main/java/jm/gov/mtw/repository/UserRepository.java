package jm.gov.mtw.repository;

import jm.gov.mtw.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByTrn(String trn);

    boolean existsByTrn(String trn);

    Optional<User> findByEmail(String email);
    Optional<User> findByTrnAndFirstNameIgnoreCaseAndLastNameIgnoreCase(String trn, String firstName, String lastName);
}