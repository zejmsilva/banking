package com.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    @Test
    @DisplayName("Branch: Depósito com valor nulo deve lançar NullPointerException")
    void testDepositNull() {
        assertThrows(NullPointerException.class, () -> account.deposit(null));
    }

    @Test
    @DisplayName("Branch: Depósito com valor zero ou negativo deve lançar IllegalArgumentException")
    void testDepositNegative() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(new BigDecimal("-1.00")));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Branch: Saque com saldo insuficiente deve lançar IllegalStateException")
    void testWithdrawInsufficientBalance() {
        account.deposit(new BigDecimal("100.00"));
        assertThrows(IllegalStateException.class, () -> account.withdraw(new BigDecimal("101.00")));
    }

    @Test
    @DisplayName("Branch: Saque com sucesso deve atualizar o saldo corretamente")
    void testWithdrawSuccess() {
        account.deposit(new BigDecimal("100.00"));
        account.withdraw(new BigDecimal("40.00"));
        assertEquals(new BigDecimal("60.00"), account.getBalance());
    }
}