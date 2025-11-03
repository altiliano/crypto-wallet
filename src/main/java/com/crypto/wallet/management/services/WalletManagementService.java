package com.crypto.wallet.management.services;

import com.crypto.wallet.management.dto.AssetDto;
import com.crypto.wallet.management.dto.WalletDto;
import com.crypto.wallet.management.exceptions.WalletNotFoundException;

public interface WalletManagementService {
    WalletDto create(String email);

    WalletDto addAsset(String email, AssetDto newAsset) throws WalletNotFoundException;

    WalletDto getWallet(String email) throws WalletNotFoundException;
}
