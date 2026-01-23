
package com.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes exaustivos para TransferService com foco em:
 *  - Branch coverage de validações (from/to nulos, mesma conta, amount nulo/zero/negativo)
 *  - Casos de borda (0, 0.01, transferência = saldo, sequências)
 *  - Invariantes: sem efeitos parciais caso ocorra exceção (atomicidade)
 *  - Precisão/escala do BigDecimal e valores muito grandes
 */
@DisplayName("TransferService - Testes Exaustivos (Borda, Branch & Invariantes)")
class TransferServiceExhaustiveTest {

    private TransferService service;
    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        service = new TransferService();
        from = new Account();
        to = new Account();
    }

    /** Compara BigDecimal ignorando diferenças de escala. */
    private static void assertBD(String esperado, BigDecimal atual) {
        assertEquals(0, atual.compareTo(new BigDecimal(esperado)),
                () -> "Esperado: " + esperado + " | Obtido: " + atual.toPlainString());
    }

    // ===== validações de argumentos =====
    @Nested
    @DisplayName("Validações de argumentos")
    class ArgumentValidation {

        @Test
        @DisplayName("Branch: Conta de origem nula deve lançar NullPointerException (saldo do 'to' inalterado)")
        void transfer_fromNull_throwsNPE_andDoesNotChangeTo() {
            // garantir estado inicial conhecido do 'to'
            assertBD("0.00", to.getBalance());
            assertThrows(NullPointerException.class,
                    () -> service.transfer(null, to, BigDecimal.ONE));
            // nenhum efeito colateral
            assertBD("0.00", to.getBalance());
        }

        @Test
        @DisplayName("Branch: Conta de destino nula deve lançar NullPointerException (saldo do 'from' inalterado)")
        void transfer_toNull_throwsNPE_andDoesNotChangeFrom() {
            from.deposit(new BigDecimal("123.45"));
            BigDecimal before = from.getBalance();
            assertThrows(NullPointerException.class,
                    () -> service.transfer(from, null, BigDecimal.ONE));
            // nenhum efeito colateral em 'from'
            assertEquals(0, from.getBalance().compareTo(before));
        }

        @Test
        @DisplayName("Branch: Transferência para a mesma conta deve lançar IllegalArgumentException (saldo inalterado)")
        void transfer_sameAccount_throwsIAE_andNoChange() {
            Account a = new Account();
            a.deposit(new BigDecimal("100.00"));
            BigDecimal before = a.getBalance();
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(a, a, new BigDecimal("10.00")));
            assertEquals(0, a.getBalance().compareTo(before));
        }

        @Test
        @DisplayName("Branch: Valor nulo deve lançar IllegalArgumentException (saldos inalterados)")
        void transfer_nullAmount_throwsIAE() {
            from.deposit(new BigDecimal("10.00"));
            BigDecimal bf = from.getBalance();
            BigDecimal bt = to.getBalance();
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(from, to, null));
            assertEquals(0, from.getBalance().compareTo(bf));
            assertEquals(0, to.getBalance().compareTo(bt));
        }

        @Test
        @DisplayName("Branch: Valor zero deve lançar IllegalArgumentException (fronteira)")
        void transfer_zero_throwsIAE() {
            from.deposit(new BigDecimal("10.00"));
            BigDecimal bf = from.getBalance();
            BigDecimal bt = to.getBalance();
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(from, to, BigDecimal.ZERO));
            assertEquals(0, from.getBalance().compareTo(bf));
            assertEquals(0, to.getBalance().compareTo(bt));
        }

        @Test
        @DisplayName("Branch: Valor negativo deve lançar IllegalArgumentException")
        void transfer_negative_throwsIAE() {
            from.deposit(new BigDecimal("10.00"));
            BigDecimal bf = from.getBalance();
            BigDecimal bt = to.getBalance();
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer(from, to, new BigDecimal("-0.01")));
            assertEquals(0, from.getBalance().compareTo(bf));
            assertEquals(0, to.getBalance().compareTo(bt));
        }
    }

    // ===== casos de borda e caminho feliz =====
    @Nested
    @DisplayName("Casos de borda & caminho feliz")
    class EdgeCasesAndHappyPath {

        @Test
        @DisplayName("Branch: Transferência mínima positiva (0.01) move saldo com precisão")
        void transfer_minPositive_movesPrecisely() {
            from.deposit(new BigDecimal("0.01"));
            service.transfer(from, to, new BigDecimal("0.01"));
            assertBD("0.00", from.getBalance());
            assertBD("0.01", to.getBalance());
        }

        @Test
        @DisplayName("Branch: Transferência válida movimenta saldos corretamente (mata VoidMethodCallMutator)")
        void transfer_valid_updatesBothBalances() {
            from.deposit(new BigDecimal("200.00"));
            service.transfer(from, to, new BigDecimal("75.00"));
            assertBD("125.00", from.getBalance());
            assertBD("75.00", to.getBalance());
        }

        @Test
        @DisplayName("Borda: Transferência exatamente igual ao saldo zera a conta de origem")
        void transfer_exactBalance_toZero() {
            from.deposit(new BigDecimal("99.99"));
            service.transfer(from, to, new BigDecimal("99.99"));
            assertBD("0.00", from.getBalance());
            assertBD("99.99", to.getBalance());
        }

        @Test
        @DisplayName("Borda: Sequência de transferências pequenas preserva precisão")
        void transfer_sequence_preservesPrecision() {
            from.deposit(new BigDecimal("1.00"));
            service.transfer(from, to, new BigDecimal("0.01")); // 0.99
            service.transfer(from, to, new BigDecimal("0.02")); // 0.97
            service.transfer(from, to, new BigDecimal("0.03")); // 0.94
            service.transfer(from, to, new BigDecimal("0.04")); // 0.90
            assertBD("0.90", from.getBalance());
            assertBD("0.10", to.getBalance());
        }

        @Test
        @DisplayName("Borda: Escalas diferentes de BigDecimal são aceitas (1, 1.0, 1.00)")
        void transfer_mixedScales_accepted() {
            from.deposit(new BigDecimal("3.00"));
            service.transfer(from, to, new BigDecimal("1"));
            service.transfer(from, to, new BigDecimal("1.0"));
            service.transfer(from, to, new BigDecimal("1.00"));
            assertBD("0.00", from.getBalance());
            assertBD("3.00", to.getBalance());
        }

        @Test
        @DisplayName("Borda: Valor muito grande é suportado (sem overflow; BigDecimal)")
        void transfer_veryLarge_supported() {
            BigDecimal huge = new BigDecimal("9999999999999999999999999999.99");
            from.deposit(huge);
            service.transfer(from, to, huge);
            assertBD("0.00", from.getBalance());
            assertEquals(0, to.getBalance().compareTo(huge));
        }
    }

    // ===== falhas no saque de origem e atomicidade =====
    @Nested
    @DisplayName("Falhas e atomicidade (sem efeitos parciais)")
    class FailuresAndAtomicity {

        @Test
        @DisplayName("Branch: Saldo insuficiente na origem → IllegalStateException e nenhum efeito parcial")
        void transfer_insufficientFunds_throwsISE_andNoPartialEffects() {
            from.deposit(new BigDecimal("10.00"));
            BigDecimal bf = from.getBalance();
            BigDecimal bt = to.getBalance();

            // withdraw da origem lançará IllegalStateException
            assertThrows(IllegalStateException.class,
                    () -> service.transfer(from, to, new BigDecimal("10.01")));

            // atomicidade: sem efeito colateral
            assertEquals(0, from.getBalance().compareTo(bf));
            assertEquals(0, to.getBalance().compareTo(bt));
        }

        @Test
        @DisplayName("Borda: Após exceção por entrada inválida, ambos os saldos permanecem inalterados")
        void noSideEffects_onInvalidInput() {
            from.deposit(new BigDecimal("50.00"));
            BigDecimal bf = from.getBalance();
            BigDecimal bt = to.getBalance();

            // inválidos: null / zero / negativo
            assertThrows(IllegalArgumentException.class, () -> service.transfer(from, to, null));
            assertThrows(IllegalArgumentException.class, () -> service.transfer(from, to, BigDecimal.ZERO));
            assertThrows(IllegalArgumentException.class, () -> service.transfer(from, to, new BigDecimal("-1.00")));

            // nenhuma alteração
            assertEquals(0, from.getBalance().compareTo(bf));
            assertEquals(0, to.getBalance().compareTo(bt));
        }
    }
}

