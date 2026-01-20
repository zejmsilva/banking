package com.bank;

import java.math.BigDecimal;

/**
 * Serviço de transferência entre contas.
 */
public class TransferService {
  public void transfer(Account from, Account to, BigDecimal amount) {
    if (from == null || to == null) throw new NullPointerException("Conta nula");
    if (from == to) throw new IllegalArgumentException("Mesma conta");
    if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Valor inválido");
    from.withdraw(amount);
    to.deposit(amount);
  }
}