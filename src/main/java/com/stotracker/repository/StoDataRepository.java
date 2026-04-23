package com.stotracker.repository;

import com.stotracker.model.StoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoDataRepository extends JpaRepository<StoData, Long> {

    List<StoData> findAllByOrderByNameAsc();

    Optional<StoData> findByName(String name);

    void deleteByName(String name);
}