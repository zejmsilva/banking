import java.math.BigDecimal;
import java.util.Objects;

/**
 * Conta bancária simples para a atividade de teste.
 * Regras principais:
 *  - Depósitos e saques devem ser > 0
 *  - Saque não pode exceder o saldo
 */

public class Account {
  private BigDecimal balance = BigDecimal.ZERO;

  public BigDecimal getBalance() { return balance; }

  public void deposit(BigDecimal amount) {
    require(amount, "Depósito inválido");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor deve ser > 0");
    }
    balance = balance.add(amount);
  }

  public void withdraw(BigDecimal amount) {
    require(amount, "Saque inválido");
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Valor deve ser > 0");
    }
    if (balance.compareTo(amount) < 0) {
      throw new IllegalStateException("Saldo insuficiente");
    }
    balance = balance.subtract(amount);
  }

  private void require(BigDecimal amount, String msg) {
    if (Objects.isNull(amount)) {
      throw new NullPointerException(msg);
    }
  }
}