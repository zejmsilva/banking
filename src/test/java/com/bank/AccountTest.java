package com.bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal; // Importação necessária

class AccountTest {
    @Test
    void testDeposit() {
        Account acc = new Account(); // O construtor padrão não recebe argumentos
        acc.deposit(new BigDecimal("100.0")); // BigDecimal exige este formato
        assertEquals(new BigDecimal("100.0"), acc.getBalance());
    }
}