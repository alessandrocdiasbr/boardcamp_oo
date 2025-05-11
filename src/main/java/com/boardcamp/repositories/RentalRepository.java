package com.boardcamp.repositories;

import com.boardcamp.models.RentalModel;
import com.boardcamp.models.GameModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface RentalRepository extends JpaRepository<RentalModel, Long>, QueryByExampleExecutor<RentalModel> {
    long countByGameAndReturnDateIsNull(GameModel game);
}
