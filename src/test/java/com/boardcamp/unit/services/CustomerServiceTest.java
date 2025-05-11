package com.boardcamp.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import com.boardcamp.dtos.CustomerDTO;
import com.boardcamp.models.CustomerModel;
import com.boardcamp.repositories.CustomerRepository;
import com.boardcamp.services.CustomerService;

class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    private CustomerDTO validCustomerDTO;
    private CustomerModel validCustomerModel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validCustomerDTO = new CustomerDTO("João Silva", "11999999999", "12345678901");
        validCustomerModel = new CustomerModel(1L, "João Silva", "11999999999", "12345678901");
    }

    @Test
    void listCustomers_ShouldReturnAllCustomers() {
      
        List<CustomerModel> expectedCustomers = Arrays.asList(
            new CustomerModel(1L, "João Silva", "11999999999", "12345678901"),
            new CustomerModel(2L, "Maria Oliveira", "21988888888", "98765432109")
        );
        
        when(customerRepository.findAll()).thenReturn(expectedCustomers);

        List<CustomerModel> result = customerService.listCustomers();

        assertEquals(expectedCustomers.size(), result.size());
        assertEquals(expectedCustomers, result);
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomer_WithValidId_ShouldReturnCustomer() {
       
        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(validCustomerModel));

     
        CustomerModel result = customerService.getCustomer(customerId);

       
        assertNotNull(result);
        assertEquals(customerId, result.getId());
        assertEquals(validCustomerModel.getName(), result.getName());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getCustomer_WithInvalidId_ShouldThrowException() {
       
        Long invalidId = 999L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

       
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> customerService.getCustomer(invalidId)
        );
        
        assertTrue(exception.getMessage().contains("Cliente não encontrado"));
        verify(customerRepository, times(1)).findById(invalidId);
    }

    @Test
    void createCustomer_WithValidData_ShouldReturnCreatedCustomer() {
        
        when(customerRepository.existsByCpf(anyString())).thenReturn(false);
        when(customerRepository.save(any(CustomerModel.class))).thenReturn(validCustomerModel);

        
        CustomerModel result = customerService.createCustomer(validCustomerDTO);

        
        assertNotNull(result);
        assertEquals(validCustomerModel.getId(), result.getId());
        assertEquals(validCustomerModel.getName(), result.getName());
        assertEquals(validCustomerModel.getCpf(), result.getCpf());
        verify(customerRepository, times(1)).existsByCpf(validCustomerDTO.getCpf());
        verify(customerRepository, times(1)).save(any(CustomerModel.class));
    }

    @Test
    void createCustomer_WithExistingCpf_ShouldThrowException() {
       
        when(customerRepository.existsByCpf(validCustomerDTO.getCpf())).thenReturn(true);

       
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> customerService.createCustomer(validCustomerDTO)
        );
        
        assertTrue(exception.getMessage().contains("CPF já cadastrado"));
        verify(customerRepository, times(1)).existsByCpf(validCustomerDTO.getCpf());
        verify(customerRepository, never()).save(any(CustomerModel.class));
    }

    @Test
    void createCustomer_WithInvalidCpf_ShouldThrowException() {
      
        CustomerDTO invalidDTO = new CustomerDTO("João Silva", "11999999999", "123");

       
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> customerService.createCustomer(invalidDTO)
        );
        
        assertTrue(exception.getMessage().contains("CPF inválido"));
        verify(customerRepository, never()).existsByCpf(anyString());
        verify(customerRepository, never()).save(any(CustomerModel.class));
    }
}