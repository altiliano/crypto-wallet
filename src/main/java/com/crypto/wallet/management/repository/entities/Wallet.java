package com.crypto.wallet.management.repository.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Asset> assets = new ArrayList<>();

    public Wallet(String email) {
        this.email = email;
    }


    public void addAsset(Asset asset) {
        assets.add(asset);
        asset.setWallet(this);
    }

    public void removeAsset(Asset asset) {
        assets.remove(asset);
        asset.setWallet(null);
    }
}
