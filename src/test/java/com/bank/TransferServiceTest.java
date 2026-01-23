
package com.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade focados em maximizar Branch Coverage da classe TransferService.
 * Branches cobertos:
 *  - from == null || to == null  -> NPE
 *  - from == to                  -> IAE
 *  - amount == null              -> IAE
 *  - amount <= 0                 -> IAE
 *  - caminho feliz (transferência válida)
 */
@DisplayName("TransferService - Branch Coverage")
class TransferServiceTest {

    private TransferService service;
    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        service = new TransferService();
        from = new Account();
        to = new Account();
    }

    // ===== validações de argumentos =====

    @Test
    @DisplayName("Branch: Conta de origem nula deve lançar NullPointerException")
    void transfer_fromNull_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> service.transfer(null, to, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Branch: Conta de destino nula deve lançar NullPointerException")
    void transfer_toNull_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> service.transfer(from, null, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Branch: Transferência para a mesma conta deve lançar IllegalArgumentException")
    void transfer_sameAccount_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(from, from, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Branch: Valor de transferência nulo deve lançar IllegalArgumentException")
    void transfer_nullAmount_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(from, to, null));
    }

    @Test
    @DisplayName("Branch: Valor de transferência zero deve lançar IllegalArgumentException")
    void transfer_zeroAmount_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(from, to, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Branch: Valor de transferência negativo deve lançar IllegalArgumentException")
    void transfer_negativeAmount_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(from, to, new BigDecimal("-10.00")));
    }

    // ===== caminho feliz =====

    @Test
    @DisplayName("Branch: Transferência válida deve mover saldo entre contas corretamente")
    void transfer_validAmount_movesBalances() {
        from.deposit(new BigDecimal("200.00"));
        service.transfer(from, to, new BigDecimal("75.00"));

        assertAll(
            () -> assertEquals(new BigDecimal("125.00"), from.getBalance()),
            () -> assertEquals(new BigDecimal("75.00"), to.getBalance())
        );
    }
}
