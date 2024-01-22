package com.algotic.data.repositories;

import com.algotic.data.entities.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactUsDetailsRepo extends JpaRepository<ContactUs, Integer> {}
