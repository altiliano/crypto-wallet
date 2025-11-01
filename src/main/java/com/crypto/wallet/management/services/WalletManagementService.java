package com.crypto.wallet.management.services;

import com.crypto.wallet.management.dto.WalletDto;

public interface WalletManagementService {
    WalletDto create(String email);
}
