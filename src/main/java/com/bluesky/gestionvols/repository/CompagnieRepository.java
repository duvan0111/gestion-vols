package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Compagnie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompagnieRepository extends JpaRepository<Compagnie, Integer> {
    Page<Compagnie> findAll(Pageable pageable);
}
