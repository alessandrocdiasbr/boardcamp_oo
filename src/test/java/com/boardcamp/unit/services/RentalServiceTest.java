package com.boardcamp.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import com.boardcamp.dtos.RentalDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.models.GameModel;
import com.boardcamp.models.RentalModel;
import com.boardcamp.repositories.CustomerRepository;
import com.boardcamp.repositories.GameRepository;
import com.boardcamp.repositories.RentalRepository;
import com.boardcamp.services.RentalService;

public class RentalServiceTest {

    @InjectMocks
    private RentalService rentalService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private GameRepository gameRepository;

    private RentalDTO validRentalDTO;
    private CustomerModel customer;
    private GameModel game;
    private RentalModel validRentalModel;
    private RentalModel finishedRentalModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new CustomerModel(1L, "João Silva", "11999999999", "12345678901");
        game = new GameModel(1L, "Monopoly", "http://image.url", 5, 1500);
        validRentalDTO = new RentalDTO(1L, 1L, 3);
        
        LocalDate rentDate = LocalDate.now();
        validRentalModel = new RentalModel(1L, customer, game, rentDate, 3, null, 4500, 0);
        
        finishedRentalModel = new RentalModel(1L, customer, game, rentDate.minusDays(3), 3, 
                                             rentDate, 4500, 0);
    }

    @Test
    void listRentals_ShouldReturnAllRentals() {
        // Arrange
        List<RentalModel> expectedRentals = Arrays.asList(validRentalModel, finishedRentalModel);
        when(rentalRepository.findAll()).thenReturn(expectedRentals);

        // Act
        List<RentalModel> result = rentalService.listRentals();

        // Assert
        assertEquals(expectedRentals.size(), result.size());
        assertEquals(expectedRentals, result);
        verify(rentalRepository, times(1)).findAll();
    }

    @Test
    void createRental_WithValidData_ShouldReturnCreatedRental() {
        // Arrange
        when(customerRepository.findById(validRentalDTO.getCustomerId())).thenReturn(Optional.of(customer));
        when(gameRepository.findById(validRentalDTO.getGameId())).thenReturn(Optional.of(game));
        when(rentalRepository.countByGameAndReturnDateIsNull(game)).thenReturn(0L);
        when(rentalRepository.save(any(RentalModel.class))).thenReturn(validRentalModel);

        // Act
        RentalModel result = rentalService.createRental(validRentalDTO);

        // Assert
        assertNotNull(result);
        assertEquals(validRentalModel.getId(), result.getId());
        assertEquals(validRentalModel.getCustomer(), result.getCustomer());
        assertEquals(validRentalModel.getGame(), result.getGame());
        assertEquals(validRentalModel.getDaysRented(), result.getDaysRented());
        assertEquals(validRentalModel.getOriginalPrice(), result.getOriginalPrice());
        verify(customerRepository, times(1)).findById(validRentalDTO.getCustomerId());
        verify(gameRepository, times(1)).findById(validRentalDTO.getGameId());
        verify(rentalRepository, times(1)).countByGameAndReturnDateIsNull(game);
        verify(rentalRepository, times(1)).save(any(RentalModel.class));
    }

    @Test
    void createRental_WithInvalidCustomerId_ShouldThrowException() {
        // Arrange
        when(customerRepository.findById(validRentalDTO.getCustomerId())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.createRental(validRentalDTO)
        );
        
        assertTrue(exception.getMessage().contains("Cliente não encontrado"));
        verify(customerRepository, times(1)).findById(validRentalDTO.getCustomerId());
        verify(gameRepository, never()).findById(any());
        verify(rentalRepository, never()).countByGameAndReturnDateIsNull(any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void createRental_WithInvalidGameId_ShouldThrowException() {
        // Arrange
        when(customerRepository.findById(validRentalDTO.getCustomerId())).thenReturn(Optional.of(customer));
        when(gameRepository.findById(validRentalDTO.getGameId())).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.createRental(validRentalDTO)
        );
        
        assertTrue(exception.getMessage().contains("Jogo não encontrado"));
        verify(customerRepository, times(1)).findById(validRentalDTO.getCustomerId());
        verify(gameRepository, times(1)).findById(validRentalDTO.getGameId());
        verify(rentalRepository, never()).countByGameAndReturnDateIsNull(any());
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void createRental_WithNoStockAvailable_ShouldThrowException() {
        // Arrange
        when(customerRepository.findById(validRentalDTO.getCustomerId())).thenReturn(Optional.of(customer));
        when(gameRepository.findById(validRentalDTO.getGameId())).thenReturn(Optional.of(game));
        when(rentalRepository.countByGameAndReturnDateIsNull(game)).thenReturn(5L); // All games rented

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.createRental(validRentalDTO)
        );
        
        assertTrue(exception.getMessage().contains("Sem estoque disponível"));
        verify(customerRepository, times(1)).findById(validRentalDTO.getCustomerId());
        verify(gameRepository, times(1)).findById(validRentalDTO.getGameId());
        verify(rentalRepository, times(1)).countByGameAndReturnDateIsNull(game);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finalizeRental_WithValidId_ShouldReturnFinalizedRental() {
        // Arrange
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(validRentalModel));
        
        RentalModel expectedFinalized = new RentalModel(
            validRentalModel.getId(),
            validRentalModel.getCustomer(),
            validRentalModel.getGame(),
            validRentalModel.getRentDate(),
            validRentalModel.getDaysRented(),
            LocalDate.now(),
            validRentalModel.getOriginalPrice(),
            0 // No delay fee
        );
        when(rentalRepository.save(any(RentalModel.class))).thenReturn(expectedFinalized);

        // Act
        RentalModel result = rentalService.finalizeRental(rentalId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedFinalized.getId(), result.getId());
        assertNotNull(result.getReturnDate());
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalRepository, times(1)).save(any(RentalModel.class));
    }

    @Test
    void finalizeRental_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long invalidId = 999L;
        when(rentalRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.finalizeRental(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Aluguel não encontrado"));
        verify(rentalRepository, times(1)).findById(invalidId);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void finalizeRental_AlreadyFinalized_ShouldThrowException() {
        // Arrange
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(finishedRentalModel));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.finalizeRental(rentalId)
        );
        
        assertTrue(exception.getMessage().contains("Aluguel já finalizado"));
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    void deleteRental_WithValidId_ShouldDeleteRental() {
        // Arrange
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(finishedRentalModel));
        doNothing().when(rentalRepository).delete(any(RentalModel.class));

        // Act
        rentalService.deleteRental(rentalId);

        // Assert
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalRepository, times(1)).delete(finishedRentalModel);
    }

    @Test
    void deleteRental_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long invalidId = 999L;
        when(rentalRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.deleteRental(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Aluguel não encontrado"));
        verify(rentalRepository, times(1)).findById(invalidId);
        verify(rentalRepository, never()).delete(any());
    }

    @Test
    void deleteRental_NotFinalized_ShouldThrowException() {
        // Arrange
        Long rentalId = 1L;
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(validRentalModel)); // not finalized

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> rentalService.deleteRental(rentalId)
        );
        
        assertTrue(exception.getMessage().contains("Aluguel não finalizado"));
        verify(rentalRepository, times(1)).findById(rentalId);
        verify(rentalRepository, never()).delete(any());
    }
}