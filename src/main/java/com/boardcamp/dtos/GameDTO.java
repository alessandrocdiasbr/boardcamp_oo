package com.boardcamp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    
    private String name;
    private String image;
    private Integer stockTotal;
    private Integer pricePerDay;
}
