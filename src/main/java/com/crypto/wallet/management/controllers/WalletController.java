package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.service.WalletSimulationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletSimulationService walletSimulationService;

    public WalletController(WalletSimulationService walletSimulationService) {
        this.walletSimulationService = walletSimulationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletDto createWallet() {
        return new WalletDto("123", "test@example.com", BigDecimal.ZERO, List.of());
    }

    @PostMapping("/simulate")
    public ResponseEntity<SimulationResponse> simulateWalletProfit(@RequestBody @Valid SimulationRequest request) {

        SimulationResponse response = walletSimulationService.simulateWalletPerformance(request);
        return ResponseEntity.ok(response);
    }
}
