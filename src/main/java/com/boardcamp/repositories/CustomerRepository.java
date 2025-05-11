package com.boardcamp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boardcamp.models.CustomerModel;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, Long> {
    boolean existsByCpf(String cpf);

    CustomerModel findByCpf(String cpf);

}
