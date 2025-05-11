package com.boardcamp.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.boardcamp.dtos.RentalDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.models.GameModel;
import com.boardcamp.models.RentalModel;
import com.boardcamp.repositories.CustomerRepository;
import com.boardcamp.repositories.GameRepository;
import com.boardcamp.repositories.RentalRepository;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;
    private final GameRepository gameRepository;

    public RentalService(RentalRepository rentalRepository, CustomerRepository customerRepository, GameRepository gameRepository) {
        this.rentalRepository = rentalRepository;
        this.customerRepository = customerRepository;
        this.gameRepository = gameRepository;
    }

    public List<RentalModel> listRentals() {
        return rentalRepository.findAll();
    }

    public RentalModel createRental(RentalDTO dto) {
        if (dto.getCustomerId() == null || dto.getGameId() == null || dto.getDaysRented() == null || dto.getDaysRented() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inválidos");
        }

        CustomerModel customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        GameModel game = gameRepository.findById(dto.getGameId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));

        long rentedGames = rentalRepository.countByGameAndReturnDateIsNull(game);
        if (rentedGames >= game.getStockTotal()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Sem estoque disponível");
        }

        RentalModel rental = new RentalModel();
        rental.setCustomer(customer);
        rental.setGame(game);
        rental.setDaysRented(dto.getDaysRented());
        rental.setRentDate(LocalDate.now());
        rental.setOriginalPrice(dto.getDaysRented() * game.getPricePerDay());
        rental.setReturnDate(null);
        rental.setDelayFee(0);

        return rentalRepository.save(rental);
    }

    public RentalModel finalizeRental(Long id) {
        RentalModel rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));

        if (rental.getReturnDate() != null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Aluguel já finalizado");
        }

        LocalDate today = LocalDate.now();
        rental.setReturnDate(today);

        LocalDate expectedReturn = rental.getRentDate().plusDays(rental.getDaysRented());
        long delayDays = ChronoUnit.DAYS.between(expectedReturn, today);
        rental.setDelayFee((int) Math.max(delayDays, 0) * rental.getGame().getPricePerDay());

        return rentalRepository.save(rental);
    }

    public void deleteRental(Long id) {
        RentalModel rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));

        if (rental.getReturnDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aluguel não finalizado");
        }

        rentalRepository.delete(rental);
    }
}
