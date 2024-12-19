package com.mjyoo.limitedflashsale.repository;

import com.mjyoo.limitedflashsale.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {
}