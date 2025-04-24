package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Aeroplane;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AeroplaneRepository extends JpaRepository<Aeroplane, Integer> {
    Page<Aeroplane> findAll(Pageable pageable);
    List<Aeroplane> findByCompagnieId(Integer compagnieId);
}
