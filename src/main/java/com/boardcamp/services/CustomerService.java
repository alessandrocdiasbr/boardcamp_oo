package com.boardcamp.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.boardcamp.dtos.CustomerDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.repositories.CustomerRepository;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<CustomerModel> listCustomers() {
        return repository.findAll();
    }

    public CustomerModel getCustomer(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    public CustomerModel createCustomer(CustomerDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome é obrigatório");

        if (!dto.getCpf().matches("\\d{11}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido");

        if (!dto.getPhone().matches("\\d{10,11}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone inválido");

        if (repository.existsByCpf(dto.getCpf()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");

        CustomerModel customer = new CustomerModel(null, dto.getName(), dto.getPhone(), dto.getCpf());
        return repository.save(customer);
    }
}
