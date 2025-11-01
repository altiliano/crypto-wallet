package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.WalletDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletDto createWallet() {

        return new WalletDto("123", BigDecimal.ZERO, List.of());
    }
}
