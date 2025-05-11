package com.boardcamp.integration.controllers;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.boardcamp.dtos.RentalDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.models.GameModel;
import com.boardcamp.models.RentalModel;
import com.boardcamp.repositories.CustomerRepository;
import com.boardcamp.repositories.GameRepository;
import com.boardcamp.repositories.RentalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private GameRepository gameRepository;

    private CustomerModel customer;
    private GameModel game;
    private RentalDTO validRentalDTO;

    @BeforeEach
    void setUp() {
       
        rentalRepository.deleteAll();
        customerRepository.deleteAll();
        gameRepository.deleteAll();

        customer = customerRepository.save(
            new CustomerModel(null, "Cliente Teste", "11999999999", "12345678901"));
        
        game = gameRepository.save(
            new GameModel(null, "Jogo Teste", "http://image.url", 5, 1500));

     
        validRentalDTO = new RentalDTO(customer.getId(), game.getId(), 3);
    }

    @Test
    void createRental_WithValidData_ReturnsCreatedStatus() throws Exception {
      
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRentalDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customer.id").value(customer.getId()))
                .andExpect(jsonPath("$.game.id").value(game.getId()))
                .andExpect(jsonPath("$.daysRented").value(validRentalDTO.getDaysRented()))
                .andExpect(jsonPath("$.originalPrice").value(game.getPricePerDay() * validRentalDTO.getDaysRented()))
                .andExpect(jsonPath("$.returnDate").isEmpty())
                .andExpect(jsonPath("$.delayFee").value(0));
    }

    @Test
    void createRental_WithInvalidCustomerId_ReturnsNotFoundStatus() throws Exception {
  
        RentalDTO invalidDTO = new RentalDTO(999L, game.getId(), 3);

        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cliente não encontrado"));
    }

    @Test
    void createRental_WithInvalidGameId_ReturnsNotFoundStatus() throws Exception {
      
        RentalDTO invalidDTO = new RentalDTO(customer.getId(), 999L, 3);

      
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Jogo não encontrado"));
    }

    @Test
    void listRentals_ReturnsAllRentals() throws Exception {
   
        RentalModel rental = new RentalModel(
            null, customer, game, LocalDate.now(), 3, null, 4500, 0);
        rentalRepository.save(rental);

        mockMvc.perform(get("/rentals")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customer.id").value(customer.getId()))
                .andExpect(jsonPath("$[0].game.id").value(game.getId()));
    }

    @Test
    void finalizeRental_WithValidId_ReturnsOkStatus() throws Exception {
   
        RentalModel rental = new RentalModel(
            null, customer, game, LocalDate.now(), 3, null, 4500, 0);
        RentalModel savedRental = rentalRepository.save(rental);

    
        mockMvc.perform(post("/rentals/" + savedRental.getId() + "/return")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRental.getId()))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());
    }

    @Test
    void finalizeRental_WithInvalidId_ReturnsNotFoundStatus() throws Exception {
  
        mockMvc.perform(post("/rentals/999/return")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Aluguel não encontrado"));
    }

    @Test
    void deleteRental_WithValidId_ReturnsNoContentStatus() throws Exception {
   
        RentalModel rental = new RentalModel(
            null, customer, game, LocalDate.now(), 3, LocalDate.now(), 4500, 0);
        RentalModel savedRental = rentalRepository.save(rental);

 
        mockMvc.perform(delete("/rentals/" + savedRental.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}