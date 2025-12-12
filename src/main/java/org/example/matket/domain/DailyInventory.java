package org.example.matket.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_inventories")
public class DailyInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private LocalDate date;

    private Integer initialStock;

    private Integer reservedQty;

    private Integer remainingQty;
}
