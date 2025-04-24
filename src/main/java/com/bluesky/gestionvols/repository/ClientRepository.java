package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    
    Optional<Client> findByEmail(String email);
    
    List<Client> findByLastName(String lastName);
    
    List<Client> findByFirstName(String firstName);
    
    List<Client> findByLastNameAndFirstName(String lastName, String firstName);
}
