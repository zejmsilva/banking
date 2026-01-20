import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        Account contaCorrente = new Account();
        Account contaPoupanca = new Account();
        TransferService servico = new TransferService();

        // 1. Depositar dinheiro
        contaCorrente.deposit(new BigDecimal("1000.00"));
        System.out.println("Saldo CC: " + contaCorrente.getBalance());

        // 2. Transferir
        servico.transfer(contaCorrente, contaPoupanca, new BigDecimal("300.00"));

        System.out.println("Saldo CC após transferência: " + contaCorrente.getBalance());
        System.out.println("Saldo Poupança: " + contaPoupanca.getBalance());
    }
}
