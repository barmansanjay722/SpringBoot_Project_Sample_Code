package com.algotic.data.repositories;

import com.algotic.data.entities.Webhooks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhooksRepo extends JpaRepository<Webhooks, Integer> {}
