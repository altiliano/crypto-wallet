package com.crypto.wallet.management.exceptions;

public class InvalidSymbolForAssetException extends RuntimeException {

    public InvalidSymbolForAssetException(String symbol) {
        super("Price not found for symbol: " + symbol + ". Please verify the symbol exists on CoinCap.");
    }

    public InvalidSymbolForAssetException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSymbolForAssetException(Throwable cause) {
        super(cause);
    }
}
