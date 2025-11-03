package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.AssetDto;
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

    @GetMapping("/simulate")
    public ResponseEntity<SimulationResponse> simulateWalletProfit(@RequestBody @Valid SimulationRequest request) {

        SimulationResponse response = walletSimulationService.simulateWalletPerformance(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{email}/assets")
    public ResponseEntity<WalletDto> addAssetToWallet(
            @PathVariable String email,
            @RequestBody @Valid AssetDto assetDto) {

        WalletDto updatedWallet = walletManagementService.addAsset(email, assetDto);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/info/{email}")
    public ResponseEntity<WalletDto> getWalletInfo(@PathVariable String email) {
        WalletDto wallet = walletManagementService.getWallet(email);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"crypto-wallet-management\"}");
    }
}
