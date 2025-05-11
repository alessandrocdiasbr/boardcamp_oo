package com.boardcamp.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rental")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RentalModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerModel customer;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameModel game;

    private LocalDate rentDate;

    private Integer daysRented;

    private LocalDate returnDate;

    private Integer originalPrice;

    private Integer delayFee;
    
}
