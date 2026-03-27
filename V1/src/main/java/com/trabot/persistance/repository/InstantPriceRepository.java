package com.trabot.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.trabot.persistance.model.entities.InstantPrice;


@Repository
public interface InstantPriceRepository extends JpaRepository<InstantPrice, Long> {
}
