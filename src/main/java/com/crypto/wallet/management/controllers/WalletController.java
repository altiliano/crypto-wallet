package com.crypto.wallet.management.controllers;

import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.CreateWalletRequest;
import com.crypto.wallet.management.dto.SimulationRequest;
import com.crypto.wallet.management.dto.SimulationResponse;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.service.WalletSimulationService;
import com.crypto.wallet.management.service.WalletManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallet Management", description = "APIs for managing crypto wallets and assets")
public class WalletController {

    private final WalletSimulationService walletSimulationService;
    private final WalletManagementService walletManagementService;

    public WalletController(WalletSimulationService walletSimulationService, WalletManagementService walletManagementService) {
        this.walletSimulationService = walletSimulationService;
        this.walletManagementService = walletManagementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new wallet", description = "Creates a new crypto wallet for the specified email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Wallet already exists for this email")
    })
    public WalletDto createWallet(@RequestBody @Valid CreateWalletRequest request) {
        return walletManagementService.create(request.getEmail());
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simulate wallet profit", description = "Simulates the performance and profit of a wallet based on provided assets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Simulation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid simulation request")
    })
    public ResponseEntity<SimulationResponse> simulateWalletProfit(@RequestBody @Valid SimulationRequest request) {

        SimulationResponse response = walletSimulationService.simulateWalletPerformance(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{email}/assets")
    @Operation(summary = "Add asset to wallet", description = "Adds a cryptocurrency asset to an existing wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid asset data"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<WalletDto> addAssetToWallet(
            @Parameter(description = "Email address of the wallet owner", required = true) @PathVariable String email,
            @RequestBody @Valid AssetDto assetDto) {

        WalletDto updatedWallet = walletManagementService.addAsset(email, assetDto);
        return ResponseEntity.ok(updatedWallet);
    }

    @GetMapping("/info/{email}")
    @Operation(summary = "Get wallet information", description = "Retrieves detailed information about a wallet including all assets and current values")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<WalletDto> getWalletInfo(
            @Parameter(description = "Email address of the wallet owner", required = true) @PathVariable String email) {
        WalletDto wallet = walletManagementService.getWallet(email);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the health status of the wallet management service")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"crypto-wallet-management\"}");
    }
}
