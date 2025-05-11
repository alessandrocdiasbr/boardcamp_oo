package com.boardcamp.integration.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.boardcamp.models.GameModel;
import com.boardcamp.repositories.GameRepository;

@DataJpaTest
@ActiveProfiles("test")
public class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    private GameModel game;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
        
        game = new GameModel(null, "Banco Imobiliário", "http://image.url", 3, 1500);
    }

    @Test
    void saveGame_WithValidData_ShouldReturnGameWithId() {
      
        GameModel savedGame = gameRepository.save(game);
        
    
        assertNotNull(savedGame.getId());
        assertEquals(game.getName(), savedGame.getName());
        assertEquals(game.getImage(), savedGame.getImage());
        assertEquals(game.getStockTotal(), savedGame.getStockTotal());
        assertEquals(game.getPricePerDay(), savedGame.getPricePerDay());
    }

    @Test
    void findById_WithExistingId_ShouldReturnGame() {
     
        GameModel savedGame = gameRepository.save(game);
        
   
        Optional<GameModel> result = gameRepository.findById(savedGame.getId());
        
       
        assertTrue(result.isPresent());
        assertEquals(savedGame.getId(), result.get().getId());
        assertEquals(savedGame.getName(), result.get().getName());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        
        Optional<GameModel> result = gameRepository.findById(999L);
        
     
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByName_WithExistingName_ShouldReturnTrue() {
        
        gameRepository.save(game);
        
        
        boolean exists = gameRepository.existsByName(game.getName());
        
        
        assertTrue(exists);
    }

    @Test
    void existsByName_WithNonExistingName_ShouldReturnFalse() {
      
        boolean exists = gameRepository.existsByName("Jogo Inexistente");
        
        
        assertFalse(exists);
    }

    @Test
    void findAll_WithMultipleGames_ShouldReturnAllGames() {
     
        gameRepository.save(game);
        gameRepository.save(new GameModel(null, "Xadrez", "http://image2.url", 5, 2000));
        
     
        List<GameModel> games = gameRepository.findAll();
        
      
        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.getName().equals("Banco Imobiliário")));
        assertTrue(games.stream().anyMatch(g -> g.getName().equals("Xadrez")));
    }
}