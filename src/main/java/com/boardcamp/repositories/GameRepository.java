package com.boardcamp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boardcamp.models.GameModel;


@Repository
public interface GameRepository extends JpaRepository<GameModel, Long>{

    boolean existsByName(String name);
    
}
