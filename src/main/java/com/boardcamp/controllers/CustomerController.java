package com.boardcamp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.boardcamp.dtos.CustomerDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.services.CustomerService;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerModel> listCustomers() {
        return service.listCustomers();
    }

    @GetMapping("/{id}")
    public CustomerModel getCustomer(@PathVariable Long id) {
        return service.getCustomer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerModel createCustomer(@RequestBody CustomerDTO dto) {
        return service.createCustomer(dto);
    }

}
