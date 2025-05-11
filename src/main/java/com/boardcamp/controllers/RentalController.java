package com.boardcamp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.boardcamp.dtos.RentalDTO;
import com.boardcamp.models.RentalModel;
import com.boardcamp.services.RentalService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/rentals")
@Tag(name = "Aluguéis", description = "Endpoints para gerenciamento de aluguéis")
@Validated
public class RentalController {
    private final RentalService service;
    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    public RentalController(RentalService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os aluguéis")
    public ResponseEntity<List<RentalModel>> listRentals() {
        logger.info("Listando todos os aluguéis");
        List<RentalModel> rentals = service.listRentals();
        return ResponseEntity.ok(rentals);
    }

    @PostMapping
    @Operation(summary = "Cria um novo aluguel")
    public ResponseEntity<RentalModel> createRental(@RequestBody @Valid RentalDTO dto) {
        logger.info("Criando novo aluguel para o jogo ID: {} e cliente ID: {}", dto.getGameId(), dto.getCustomerId());
        RentalModel rental = service.createRental(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Finaliza um aluguel")
    public ResponseEntity<RentalModel> finalizeRental(@PathVariable Long id) {
        logger.info("Finalizando aluguel ID: {}", id);
    RentalModel rental = service.finalizeRental(id);        
    
    return ResponseEntity.ok(rental);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um aluguel")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        logger.info("Removendo aluguel ID: {}", id);
        service.deleteRental(id);
        return ResponseEntity.noContent().build();
    }
}