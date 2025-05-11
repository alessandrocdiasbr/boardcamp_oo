package com.boardcamp.integration.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.boardcamp.models.CustomerModel;
import com.boardcamp.models.GameModel;
import com.boardcamp.models.RentalModel;
import com.boardcamp.repositories.CustomerRepository;
import com.boardcamp.repositories.GameRepository;
import com.boardcamp.repositories.RentalRepository;

@DataJpaTest
@ActiveProfiles("test")
public class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private GameRepository gameRepository;

    private CustomerModel customer;
    private GameModel game;
    private RentalModel rental;

    @BeforeEach
    void setUp() {
        rentalRepository.deleteAll();
        customerRepository.deleteAll();
        gameRepository.deleteAll();
        
        customer = customerRepository.save(
            new CustomerModel(null, "Cliente Teste", "11999999999", "12345678901"));
        
        game = gameRepository.save(
            new GameModel(null, "Jogo Teste", "http://image.url", 5, 1500));
        
        rental = new RentalModel(null, customer, game, LocalDate.now(), 3, null, 4500, 0);
    }

    @Test
    void saveRental_WithValidData_ShouldReturnRentalWithId() {
    
        RentalModel savedRental = rentalRepository.save(rental);
        
   
        assertNotNull(savedRental.getId());
        assertEquals(rental.getCustomer().getId(), savedRental.getCustomer().getId());
        assertEquals(rental.getGame().getId(), savedRental.getGame().getId());
        assertEquals(rental.getDaysRented(), savedRental.getDaysRented());
        assertEquals(rental.getOriginalPrice(), savedRental.getOriginalPrice());
    }

    @Test
    void findById_WithExistingId_ShouldReturnRental() {
  
        RentalModel savedRental = rentalRepository.save(rental);
        
      
        Optional<RentalModel> result = rentalRepository.findById(savedRental.getId());
        
     
        assertTrue(result.isPresent());
        assertEquals(savedRental.getId(), result.get().getId());
        assertEquals(savedRental.getCustomer().getId(), result.get().getCustomer().getId());
        assertEquals(savedRental.getGame().getId(), result.get().getGame().getId());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        
        Optional<RentalModel> result = rentalRepository.findById(999L);
        
        
        assertTrue(result.isEmpty());
    }

    @Test
    void countByGameAndReturnDateIsNull_WithActiveRentals_ShouldReturnCount() {
       
        rentalRepository.save(rental);
        
 
        long count = rentalRepository.countByGameAndReturnDateIsNull(game);
        
 
        assertEquals(1, count);
    }

    @Test
    void countByGameAndReturnDateIsNull_WithFinishedRentals_ShouldReturnZero() {
     
        rental.setReturnDate(LocalDate.now());
        rentalRepository.save(rental);
     
        long count = rentalRepository.countByGameAndReturnDateIsNull(game);
     
        assertEquals(0, count);
    }

    @Test
    void findAll_WithMultipleRentals_ShouldReturnAllRentals() {
      
        RentalModel rental1 = rentalRepository.save(rental);
        
        GameModel game2 = gameRepository.save(
            new GameModel(null, "Jogo 2", "http://image2.url", 3, 2000));
            
        RentalModel rental2 = new RentalModel(
            null, customer, game2, LocalDate.now(), 5, null, 10000, 0);
        rentalRepository.save(rental2);
        
   
        List<RentalModel> rentals = rentalRepository.findAll();
        
      
        assertEquals(2, rentals.size());
        assertTrue(rentals.stream().anyMatch(r -> r.getId().equals(rental1.getId())));
        assertTrue(rentals.stream().anyMatch(r -> r.getGame().getName().equals("Jogo 2")));
    }
}