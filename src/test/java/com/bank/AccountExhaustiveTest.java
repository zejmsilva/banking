
package com.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes exaustivos de unidade para a classe Account, com foco em:
 *  - Cobertura de branches (null / zero / negativo / positivo)
 *  - Casos de borda (saque exato = saldo, sequência de operações, diferentes escalas)
 *  - Invariantes de domínio (saldo nunca negativo; operações idempotentes em caso de exceção)
 *
 * Observação: usamos comparação por compareTo (== 0) quando a escala pode variar,
 * para evitar falsos negativos devido a BigDecimal com escalas diferentes (ex.: 100.0 vs 100.00).
 */
@DisplayName("Account - Testes Exaustivos (Borda, Branch & Invariantes)")
class AccountExhaustiveTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
    }

    /** Compara BigDecimal ignorando diferenças de escala. */
    private static void assertBD(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)),
                () -> "Esperado: " + expected + " | Obtido: " + actual);
    }

    // ===== estado inicial =====
    @Test
    @DisplayName("Estado: Nova conta deve iniciar com saldo 0.00")
    void initialBalance_isZero() {
        assertBD("0.00", account.getBalance());
    }

    // ===== DEPÓSITO =====
    @Nested
    @DisplayName("Depósito")
    class Deposit {

        @Test
        @DisplayName("Branch: Depósito com valor nulo deve lançar NullPointerException")
        void deposit_null_throwsNPE() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> account.deposit(null));
            // Opcional: assertTrue(ex.getMessage().toLowerCase().contains("inválid"));
            assertBD("0.00", account.getBalance()); // saldo inalterado
        }

        @Test
        @DisplayName("Branch: Depósito com zero deve lançar IllegalArgumentException")
        void deposit_zero_throwsIAE() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> account.deposit(BigDecimal.ZERO));
            assertBD("0.00", account.getBalance()); // saldo inalterado
        }

        @Test
        @DisplayName("Branch: Depósito com negativo deve lançar IllegalArgumentException")
        void deposit_negative_throwsIAE() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> account.deposit(new BigDecimal("-1.00")));
            assertBD("0.00", account.getBalance()); // saldo inalterado
        }

        @Test
        @DisplayName("Branch: Depósito com valor positivo deve atualizar saldo (mata VoidMethodCallMutator)")
        void deposit_positive_updatesBalance() {
            account.deposit(new BigDecimal("100.00"));
            assertBD("100.00", account.getBalance());
        }

        @Test
        @DisplayName("Borda: Múltiplos depósitos pequenos preservam precisão e somam exatamente")
        void deposit_manySmall_exactSum() {
            account.deposit(new BigDecimal("0.01"));
            account.deposit(new BigDecimal("0.02"));
            account.deposit(new BigDecimal("0.03"));
            account.deposit(new BigDecimal("0.04"));
            account.deposit(new BigDecimal("0.90"));
            assertBD("1.00", account.getBalance());
        }

        @Test
        @DisplayName("Borda: Depósitos com escalas diferentes acumulam corretamente (1 e 1.0 e 1.00)")
        void deposit_mixedScales_accumulate() {
            account.deposit(new BigDecimal("1"));
            account.deposit(new BigDecimal("1.0"));
            account.deposit(new BigDecimal("1.00"));
            // 3 no total; escala pode variar internamente
            assertBD("3.00", account.getBalance());
        }

        @Test
        @DisplayName("Borda: Depósito com valor muito grande é suportado pelo BigDecimal")
        void deposit_veryLargeValue_supported() {
            // BigDecimal suporta alta precisão; aqui validamos que a soma ocorre sem overflow
            account.deposit(new BigDecimal("9999999999999999999999999999.99"));
            assertBD("9999999999999999999999999999.99", account.getBalance());
        }
    }

    // ===== SAQUE =====
    @Nested
    @DisplayName("Saque")
    class Withdraw {

        @Test
        @DisplayName("Branch: Saque com valor nulo deve lançar NullPointerException")
        void withdraw_null_throwsNPE() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> account.withdraw(null));
            assertBD("0.00", account.getBalance()); // saldo inalterado
        }

        @Test
        @DisplayName("Branch: Saque com zero deve lançar IllegalArgumentException")
        void withdraw_zero_throwsIAE() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> account.withdraw(BigDecimal.ZERO));
            assertBD("0.00", account.getBalance()); // saldo inalterado
        }

        @Test
        @DisplayName("Branch: Saque com negativo deve lançar IllegalArgumentException")
        void withdraw_negative_throwsIAE() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> account.withdraw(new BigDecimal("-5.00")));
            assertBD("0.00", account.getBalance()); // saldo inalterado
        }

        @Test
        @DisplayName("Branch: Saque maior que o saldo deve lançar IllegalStateException (saldo insuficiente)")
        void withdraw_moreThanBalance_throwsISE() {
            account.deposit(new BigDecimal("50.00"));
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> account.withdraw(new BigDecimal("100.00")));
            // importante: saldo permanece o mesmo após a exceção
            assertBD("50.00", account.getBalance());
        }

        @Test
        @DisplayName("Borda: Saque exatamente igual ao saldo resulta em saldo 0.00")
        void withdraw_exactBalance_toZero() {
            account.deposit(new BigDecimal("75.00"));
            account.withdraw(new BigDecimal("75.00"));
            assertBD("0.00", account.getBalance());
        }

        @Test
        @DisplayName("Borda: Sequência de depósitos e saques mantém saldo consistente")
        void withdraw_sequence_consistency() {
            account.deposit(new BigDecimal("100.00"));
            account.withdraw(new BigDecimal("10.00"));
            account.deposit(new BigDecimal("0.50"));
            account.withdraw(new BigDecimal("0.50"));
            account.deposit(new BigDecimal("9.50"));
            assertBD("99.50", account.getBalance());
        }

        @Test
        @DisplayName("Borda: Saques parciais sucessivos não permitem saldo negativo")
        void withdraw_partialNeverGoesNegative() {
            account.deposit(new BigDecimal("10.00"));
            account.withdraw(new BigDecimal("3.00"));   // 7.00
            account.withdraw(new BigDecimal("2.50"));   // 4.50
            account.withdraw(new BigDecimal("4.50"));   // 0.00
            assertBD("0.00", account.getBalance());
            // Próximo saque deve falhar e não alterar o saldo
            assertThrows(IllegalStateException.class, () -> account.withdraw(new BigDecimal("0.01")));
            assertBD("0.00", account.getBalance());
        }
    }

    // ===== INVARIANTES =====
    @Nested
    @DisplayName("Invariantes")
    class Invariants {

        @Test
        @DisplayName("Invariante: Após qualquer exceção, o saldo permanece inalterado")
        void invariant_balanceUnchangedOnException() {
            account.deposit(new BigDecimal("10.00"));
            BigDecimal before = account.getBalance();

            // exceções esperadas
            assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> account.withdraw(new BigDecimal("-1.00")));
            assertThrows(IllegalStateException.class,    () -> account.withdraw(new BigDecimal("999.00")));

            // saldo não muda
            assertBD(before.toPlainString(), account.getBalance());
        }

        @Test
        @DisplayName("Invariante: A ordem de depósitos resulta no mesmo saldo (comutatividade da soma)")
        void invariant_depositOrder_indifferent() {
            Account a1 = new Account();
            a1.deposit(new BigDecimal("1.10"));
            a1.deposit(new BigDecimal("2.20"));
            a1.deposit(new BigDecimal("3.30"));

            Account a2 = new Account();
            a2.deposit(new BigDecimal("3.30"));
            a2.deposit(new BigDecimal("2.20"));
            a2.deposit(new BigDecimal("1.10"));

            assertBD(a1.getBalance().toPlainString(), a2.getBalance());
        }
    }
}
