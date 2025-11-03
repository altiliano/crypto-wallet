package com.crypto.wallet.management.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WalletNotFoundExceptionTest {

    @Test
    void shouldCreateExceptionWithEmailMessage() {
        String email = "test@example.com";
        WalletNotFoundException exception = new WalletNotFoundException(email);

        assertEquals("Wallet not found for email: " + email, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Custom error message";
        Throwable cause = new IllegalStateException("Root cause");

        WalletNotFoundException exception = new WalletNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithCause() {
        Throwable cause = new IllegalStateException("Root cause");

        WalletNotFoundException exception = new WalletNotFoundException(cause);

        assertEquals(cause, exception.getCause());
    }
}
