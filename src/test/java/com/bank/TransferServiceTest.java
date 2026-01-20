package com.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("Branch: Transferência com conta de origem ou destino nula")
    void testTransferNullAccounts() {
        assertThrows(NullPointerException.class, () -> service.transfer(null, to, BigDecimal.ONE));
        assertThrows(NullPointerException.class, () -> service.transfer(from, null, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Branch: Transferência para a mesma conta deve falhar")
    void testTransferSameAccount() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer(from, from, BigDecimal.ONE));
    }

    @Test
    @DisplayName("Branch: Valor de transferência inválido (nulo, zero ou negativo)")
    void testTransferInvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> service.transfer(from, to, null));
        assertThrows(IllegalArgumentException.class, () -> service.transfer(from, to, new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Branch: Caminho feliz de transferência completa")
    void testTransferSuccess() {
        from.deposit(new BigDecimal("100.00"));
        service.transfer(from, to, new BigDecimal("30.00"));
        
        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("30.00"), to.getBalance());
    }
}
