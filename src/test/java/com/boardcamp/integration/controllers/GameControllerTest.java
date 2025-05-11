package com.boardcamp.integration.controllers;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.boardcamp.dtos.GameDTO;
import com.boardcamp.models.GameModel;
import com.boardcamp.repositories.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    private GameDTO validGameDTO;

    @BeforeEach
    void setUp() {
    
        gameRepository.deleteAll();

    
        validGameDTO = new GameDTO("Banco Imobiliário", "http://image.url", 3, 1500);
    }

    @Test
    void createGame_WithValidData_ReturnsCreatedStatus() throws Exception {
   
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validGameDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(validGameDTO.getName()))
                .andExpect(jsonPath("$.image").value(validGameDTO.getImage()))
                .andExpect(jsonPath("$.stockTotal").value(validGameDTO.getStockTotal()))
                .andExpect(jsonPath("$.pricePerDay").value(validGameDTO.getPricePerDay()));
    }

    @Test
    void createGame_WithDuplicateName_ReturnsConflictStatus() throws Exception {
        
        GameModel existingGame = new GameModel(null, validGameDTO.getName(), 
                                               validGameDTO.getImage(), 
                                               validGameDTO.getStockTotal(), 
                                               validGameDTO.getPricePerDay());
        gameRepository.save(existingGame);

     
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validGameDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Jogo já existe"));
    }

    @Test
    void createGame_WithInvalidStock_ReturnsBadRequestStatus() throws Exception {
        
        GameDTO invalidDTO = new GameDTO("Banco Imobiliário", "http://image.url", 0, 1500);

        
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Estoque inválido"));
    }

    @Test
    void createGame_WithInvalidPrice_ReturnsBadRequestStatus() throws Exception {
  
        GameDTO invalidDTO = new GameDTO("Banco Imobiliário", "http://image.url", 3, -100);

       
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Preço inválido"));
    }

    @Test
    void createGame_WithEmptyName_ReturnsBadRequestStatus() throws Exception {
        
        GameDTO invalidDTO = new GameDTO("", "http://image.url", 3, 1500);

      
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nome é obrigatório"));
    }

    @Test
    void createGame_WithInvalidImageUrl_ReturnsBadRequestStatus() throws Exception {
        
        GameDTO invalidDTO = new GameDTO("Banco Imobiliário", "invalid-url", 3, 1500);

        
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("URL da imagem inválida"));
    }

    @Test
    void createGame_WithNullImage_ReturnsBadRequestStatus() throws Exception {
    
        GameDTO invalidDTO = new GameDTO("Banco Imobiliário", null, 3, 1500);

     
        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("URL da imagem é obrigatória"));
    }

    @Test
    void listGames_ReturnsAllGames() throws Exception {
    
        gameRepository.save(new GameModel(null, "Jogo 1", "http://image1.url", 3, 1500));
        gameRepository.save(new GameModel(null, "Jogo 2", "http://image2.url", 5, 2000));

     
        mockMvc.perform(get("/games")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Jogo 1"))
                .andExpect(jsonPath("$[1].name").value("Jogo 2"));
    }
}