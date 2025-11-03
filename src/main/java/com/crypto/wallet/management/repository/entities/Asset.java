package com.crypto.wallet.management.repository.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "\"value\"", nullable = false)
    private BigDecimal value;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    public Asset(String symbol, BigDecimal quantity, BigDecimal price, Wallet wallet, BigDecimal value) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.value = value;
        this.wallet = wallet;
    }
}
