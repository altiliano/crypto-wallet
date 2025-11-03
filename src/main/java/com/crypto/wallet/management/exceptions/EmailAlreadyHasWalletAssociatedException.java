package com.crypto.wallet.management.exceptions;

public class EmailAlreadyHasWalletAssociatedException extends RuntimeException {

    public EmailAlreadyHasWalletAssociatedException(String email) {
        super("Email '" + email + "' already has a wallet associated. Each email can only have one wallet.");
    }

    public EmailAlreadyHasWalletAssociatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailAlreadyHasWalletAssociatedException(Throwable cause) {
        super(cause);
    }
}
