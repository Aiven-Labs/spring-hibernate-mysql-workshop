package io.aiven.dataworkshop;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.aiven.dataworkshop.model.Cat;

public interface CatRepository extends JpaRepository<Cat, Integer> {

    @Query("SELECT c FROM Cat c WHERE c.owner.id =?1")
    List<Cat> findCatsByOwner(Integer id);
    
}
