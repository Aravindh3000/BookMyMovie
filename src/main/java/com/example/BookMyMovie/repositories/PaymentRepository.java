package com.example.BookMyMovie.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.BookMyMovie.models.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>{
    
    Optional<Payment> findByReferenceId(String referenceId);
}
