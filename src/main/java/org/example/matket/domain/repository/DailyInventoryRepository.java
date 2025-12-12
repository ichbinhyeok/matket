package org.example.matket.domain.repository;

import org.example.matket.domain.DailyInventory;
import org.example.matket.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyInventoryRepository extends JpaRepository<DailyInventory, Long> {

    Optional<DailyInventory> findByProductAndDate(Product product, LocalDate date);
}


