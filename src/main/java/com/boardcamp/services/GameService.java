package com.boardcamp.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.boardcamp.dtos.GameDTO;
import com.boardcamp.models.GameModel;
import com.boardcamp.repositories.GameRepository;

@Service
public class GameService {
     private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    public List<GameModel> listGames() {
        return repository.findAll();
    }

    public GameModel addGame(GameDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");

        if (dto.getImage() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL da imagem é obrigatória");

        if (!dto.getImage().startsWith("http"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL da imagem inválida");

        if (dto.getStockTotal() == null || dto.getStockTotal() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque inválido");

        if (dto.getPricePerDay() == null || dto.getPricePerDay() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço inválido");

        if (repository.existsByName(dto.getName()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Jogo já existe");

        GameModel game = new GameModel(null, dto.getName(), dto.getImage(), dto.getStockTotal(), dto.getPricePerDay());
        return repository.save(game);
    }
}
