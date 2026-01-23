
package com.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes focados em maximizar Branch Coverage de Account.
 * Branches cobertos:
 *  - deposit: amount == null | amount = 0 | amount < 0 | amount > 0
 *  - withdraw: amount == null | amount = 0 | amount < 0 | balance < amount | balance >= amount
 */
@DisplayName("Account - Testes de Branch Coverage")
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    // ===== deposit =====

    @Test
    @DisplayName("Branch: Depósito com valor nulo deve lançar NullPointerException")
    void deposit_nullAmount_throwsNPE() {
        assertThrows(NullPointerException.class, () -> account.deposit(null));
    }

    @Test
    @DisplayName("Branch: Depósito com valor zero deve lançar IllegalArgumentException")
    void deposit_zeroAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Branch: Depósito com valor negativo deve lançar IllegalArgumentException")
    void deposit_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(new BigDecimal("-1.00")));
    }

    @Test
    @DisplayName("Branch: Depósito com valor positivo deve atualizar o saldo")
    void deposit_positiveAmount_updatesBalance() {
        account.deposit(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    // ===== withdraw =====

    @Test
    @DisplayName("Branch: Saque com valor nulo deve lançar NullPointerException")
    void withdraw_nullAmount_throwsNPE() {
        assertThrows(NullPointerException.class, () -> account.withdraw(null));
    }

    @Test
    @DisplayName("Branch: Saque com valor zero deve lançar IllegalArgumentException")
    void withdraw_zeroAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Branch: Saque com valor negativo deve lançar IllegalArgumentException")
    void withdraw_negativeAmount_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(new BigDecimal("-5.00")));
    }

    @Test
    @DisplayName("Branch: Saque maior que o saldo deve lançar IllegalStateException")
    void withdraw_amountGreaterThanBalance_throwsIllegalState() {
        account.deposit(new BigDecimal("50.00"));
        assertThrows(IllegalStateException.class, () -> account.withdraw(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("Branch: Saque com saldo suficiente deve atualizar o saldo corretamente")
    void withdraw_validAmount_updatesBalance() {
        account.deposit(new BigDecimal("150.00"));
        account.withdraw(new BigDecimal("40.00"));
        assertEquals(new BigDecimal("110.00"), account.getBalance());
    }
}