package com.boardcamp.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import com.boardcamp.dtos.GameDTO;
import com.boardcamp.models.GameModel;
import com.boardcamp.repositories.GameRepository;
import com.boardcamp.services.GameService;

public class GameServiceTest {

    @InjectMocks
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    private GameDTO validGameDTO;
    private GameModel validGameModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validGameDTO = new GameDTO("Monopoly", "http://image.url", 5, 1500);
        validGameModel = new GameModel(1L, "Monopoly", "http://image.url", 5, 1500);
    }

    @Test
    void listGames_ShouldReturnAllGames() {
        // Arrange
        List<GameModel> expectedGames = Arrays.asList(
            new GameModel(1L, "Monopoly", "http://image1.url", 5, 1500),
            new GameModel(2L, "Xadrez", "http://image2.url", 10, 2000)
        );
        
        when(gameRepository.findAll()).thenReturn(expectedGames);

        // Act
        List<GameModel> result = gameService.listGames();

        // Assert
        assertEquals(expectedGames.size(), result.size());
        assertEquals(expectedGames, result);
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    void addGame_WithValidData_ShouldReturnCreatedGame() {
        // Arrange
        when(gameRepository.existsByName(anyString())).thenReturn(false);
        when(gameRepository.save(any(GameModel.class))).thenReturn(validGameModel);

        // Act
        GameModel result = gameService.addGame(validGameDTO);

        // Assert
        assertNotNull(result);
        assertEquals(validGameModel.getId(), result.getId());
        assertEquals(validGameModel.getName(), result.getName());
        assertEquals(validGameModel.getStockTotal(), result.getStockTotal());
        assertEquals(validGameModel.getPricePerDay(), result.getPricePerDay());
        verify(gameRepository, times(1)).existsByName(validGameDTO.getName());
        verify(gameRepository, times(1)).save(any(GameModel.class));
    }

    @Test
    void addGame_WithExistingName_ShouldThrowException() {
        // Arrange
        when(gameRepository.existsByName(validGameDTO.getName())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> gameService.addGame(validGameDTO)
        );
        
        assertTrue(exception.getMessage().contains("Jogo já existe"));
        verify(gameRepository, times(1)).existsByName(validGameDTO.getName());
        verify(gameRepository, never()).save(any(GameModel.class));
    }

    @Test
    void addGame_WithNullName_ShouldThrowException() {
        // Arrange
        GameDTO invalidDTO = new GameDTO(null, "http://image.url", 5, 1500);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> gameService.addGame(invalidDTO)
        );
        
        assertTrue(exception.getMessage().contains("Nome é obrigatório"));
        verify(gameRepository, never()).existsByName(anyString());
        verify(gameRepository, never()).save(any(GameModel.class));
    }

    @Test
    void addGame_WithInvalidStock_ShouldThrowException() {
        // Arrange
        GameDTO invalidDTO = new GameDTO("Monopoly", "http://image.url", 0, 1500);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> gameService.addGame(invalidDTO)
        );
        
        assertTrue(exception.getMessage().contains("Estoque inválido"));
        verify(gameRepository, never()).existsByName(anyString());
        verify(gameRepository, never()).save(any(GameModel.class));
    }

    @Test
    void addGame_WithInvalidPrice_ShouldThrowException() {
        // Arrange
        GameDTO invalidDTO = new GameDTO("Monopoly", "http://image.url", 5, 0);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> gameService.addGame(invalidDTO)
        );
        
        assertTrue(exception.getMessage().contains("Preço inválido"));
        verify(gameRepository, never()).existsByName(anyString());
        verify(gameRepository, never()).save(any(GameModel.class));
    }
}