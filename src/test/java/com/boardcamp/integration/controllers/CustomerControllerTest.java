package com.boardcamp.integration.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.boardcamp.dtos.CustomerDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.repositories.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private CustomerDTO validCustomerDTO;

    @BeforeEach
    void setUp() {
       
        customerRepository.deleteAll();

       
        validCustomerDTO = new CustomerDTO("Teste da Silva", "11999999999", "12345678901");
    }

    @Test
    void createCustomer_WithValidData_ReturnsCreatedStatus() throws Exception {
    
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(validCustomerDTO.getName()))
                .andExpect(jsonPath("$.cpf").value(validCustomerDTO.getCpf()))
                .andExpect(jsonPath("$.phone").value(validCustomerDTO.getPhone()));
    }

    @Test
    void createCustomer_WithDuplicateCpf_ReturnsConflictStatus() throws Exception {
       
        CustomerModel existingCustomer = new CustomerModel(null, validCustomerDTO.getName(), 
                                                         validCustomerDTO.getPhone(), 
                                                         validCustomerDTO.getCpf());
        customerRepository.save(existingCustomer);

       
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCustomerDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CPF já cadastrado"));
    }

    @Test
    void createCustomer_WithInvalidCpf_ReturnsBadRequestStatus() throws Exception {
     
        CustomerDTO invalidDTO = new CustomerDTO("Teste da Silva", "11999999999", "123");

     
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CPF inválido"));
    }

    @Test
    void getCustomer_WithExistingId_ReturnsCustomer() throws Exception {
       
        CustomerModel savedCustomer = customerRepository.save(
            new CustomerModel(null, validCustomerDTO.getName(), 
                            validCustomerDTO.getPhone(), 
                            validCustomerDTO.getCpf())
        );

     
        mockMvc.perform(get("/customers/" + savedCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$.name").value(savedCustomer.getName()))
                .andExpect(jsonPath("$.cpf").value(savedCustomer.getCpf()))
                .andExpect(jsonPath("$.phone").value(savedCustomer.getPhone()));
    }

    @Test
    void getCustomer_WithNonExistingId_ReturnsNotFound() throws Exception {
    
        mockMvc.perform(get("/customers/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cliente não encontrado"));
    }

    @Test
    void listCustomers_ReturnsAllCustomers() throws Exception {
     
        customerRepository.save(new CustomerModel(null, "Cliente 1", "11999999999", "12345678901"));
        customerRepository.save(new CustomerModel(null, "Cliente 2", "22888888888", "98765432109"));

       
        mockMvc.perform(get("/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cliente 1"))
                .andExpect(jsonPath("$[1].name").value("Cliente 2"));
    }
}