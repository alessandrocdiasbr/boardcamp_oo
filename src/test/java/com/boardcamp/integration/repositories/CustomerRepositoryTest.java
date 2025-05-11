package com.boardcamp.integration.repositories;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.boardcamp.models.CustomerModel;
import com.boardcamp.repositories.CustomerRepository;

@DataJpaTest
@ActiveProfiles("test")
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private CustomerModel customer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        
        customer = new CustomerModel(null, "Cliente Teste", "11999999999", "12345678901");
    }

    @Test
    void saveCustomer_WithValidData_ShouldReturnCustomerWithId() {
      
        CustomerModel savedCustomer = customerRepository.save(customer);
        
     
        assertNotNull(savedCustomer.getId());
        assertEquals(customer.getName(), savedCustomer.getName());
        assertEquals(customer.getPhone(), savedCustomer.getPhone());
        assertEquals(customer.getCpf(), savedCustomer.getCpf());
    }

    @Test
    void findById_WithExistingId_ShouldReturnCustomer() {
    
        CustomerModel savedCustomer = customerRepository.save(customer);
        
       
        Optional<CustomerModel> result = customerRepository.findById(savedCustomer.getId());
        
 
        assertTrue(result.isPresent());
        assertEquals(savedCustomer.getId(), result.get().getId());
        assertEquals(savedCustomer.getName(), result.get().getName());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
       
        Optional<CustomerModel> result = customerRepository.findById(999L);
        
       
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByCpf_WithExistingCpf_ShouldReturnTrue() {
        
        customerRepository.save(customer);
        
       
        boolean exists = customerRepository.existsByCpf(customer.getCpf());
        
      
        assertTrue(exists);
    }

    @Test
    void existsByCpf_WithNonExistingCpf_ShouldReturnFalse() {
    
        boolean exists = customerRepository.existsByCpf("98765432109");
   
        assertFalse(exists);
    }
}