package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.CreateWalletRequest;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.service.WalletSimulationService;
import com.crypto.wallet.management.services.WalletManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletSimulationService walletSimulationService;
    private final WalletManagementService walletManagementService;

    public WalletController(WalletSimulationService walletSimulationService, WalletManagementService walletManagementService) {
        this.walletSimulationService = walletSimulationService;
        this.walletManagementService = walletManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletDto createWallet(@RequestBody @Valid CreateWalletRequest request) {
        return walletManagementService.create(request.getEmail());
    }

    @PostMapping("/simulate")
    public ResponseEntity<SimulationResponse> simulateWalletProfit(@RequestBody @Valid SimulationRequest request) {

        SimulationResponse response = walletSimulationService.simulateWalletPerformance(request);
        return ResponseEntity.ok(response);
    }
}
