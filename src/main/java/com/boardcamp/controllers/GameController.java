package com.boardcamp.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.boardcamp.dtos.GameDTO;
import com.boardcamp.models.GameModel;
import com.boardcamp.services.GameService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/games")
public class GameController {
    private final GameService service;
    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping
    public List<GameModel> listGames() {
        return service.listGames();
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameModel createGame(@RequestBody GameDTO dto) {
        return service.addGame(dto);
    }

}
