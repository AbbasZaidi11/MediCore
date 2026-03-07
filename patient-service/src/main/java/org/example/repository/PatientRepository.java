package org.example.repository;

import org.example.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email,UUID id);
    
//    Check if there exists a record where:
//    email = given email
//    AND id is NOT equal to given id
}
