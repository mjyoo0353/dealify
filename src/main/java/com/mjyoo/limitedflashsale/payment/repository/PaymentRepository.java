package com.mjyoo.limitedflashsale.payment.repository;

import com.mjyoo.limitedflashsale.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}