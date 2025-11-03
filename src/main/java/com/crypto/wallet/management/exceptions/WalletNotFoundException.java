package com.crypto.wallet.management.exceptions;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String email) {
        super("Wallet not found for email: " + email);
    }

    public WalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalletNotFoundException(Throwable cause) {
        super(cause);
    }
}
