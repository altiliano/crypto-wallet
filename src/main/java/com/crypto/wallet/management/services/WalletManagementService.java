package com.crypto.wallet.management.services;

import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.WalletDto;

public interface WalletManagementService {
    WalletDto create(String email);
    WalletDto addAsset(String email, AssetDto asset);
    WalletDto getWallet(String email);
}
