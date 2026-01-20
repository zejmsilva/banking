package com.bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class AccountTest {
    @Test
    void testDeposit() {
        Account acc = new Account();
        acc.deposit(new BigDecimal("100.0"));
        // Use compareTo para BigDecimal para evitar problemas de precisão no assertEquals
        assertTrue(new BigDecimal("100.0").compareTo(acc.getBalance()) == 0);
    }
}