
package com.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes exaustivos para a classe Main.
 * Foco:
 *  - Casos de borda do método main(String[] args): null, array vazio, argumentos arbitrários
 *  - Ordem e conteúdo da saída padrão (System.out)
 *  - Idempotência / ausência de efeitos colaterais entre execuções
 *
 * Expectativa (de acordo com Main):
 *   Saldo CC: 1000.00
 *   Saldo CC após transferência: 700.00
 *   Saldo Poupança: 300.00
 */
@DisplayName("Main - Testes Exaustivos (Borda e Saída)")
class MainExhaustiveTest {

    /** Executa Main.main(args) capturando System.out e retorna a saída como String. */
    private String runMainAndCapture(String[] args) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);
        try {
            Main.main(args);
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }
        return baos.toString();
    }

    /** Normaliza quebras de linha para '\n' e retorna as linhas da saída. */
    private String[] normalizeLines(String out) {
        return out.replace("\r\n", "\n")
                  .replace("\r", "\n")
                  .split("\n");
    }

    /** Verifica as três primeiras linhas esperadas, permitindo linhas extras ao final. */
    private void assertExpectedOutput(String out) {
        String[] lines = normalizeLines(out);
        assertTrue(lines.length >= 3, "Saída deve conter ao menos 3 linhas.");
        assertEquals("Saldo CC: 1000.00", lines[0], "Linha 1 incorreta");
        assertEquals("Saldo CC após transferência: 700.00", lines[1], "Linha 2 incorreta");
        assertEquals("Saldo Poupança: 300.00", lines[2], "Linha 3 incorreta");
    }

    // ===== Casos de borda para args =====

    @Test
    @DisplayName("Borda: main(null) não lança exceção e imprime saldos esperados")
    void main_nullArgs_printsExpected() {
        String out = runMainAndCapture(null);
        assertExpectedOutput(out);
    }

    @Test
    @DisplayName("Borda: main(new String[0]) não lança exceção e imprime saldos esperados")
    void main_emptyArgs_printsExpected() {
        String out = runMainAndCapture(new String[0]);
        assertExpectedOutput(out);
    }

    @Test
    @DisplayName("Borda: main com argumentos arbitrários é ignorado e imprime saldos esperados")
    void main_arbitraryArgs_printsExpected() {
        String out = runMainAndCapture(new String[] { "foo", "bar", "--whatever=123" });
        assertExpectedOutput(out);
    }

    // ===== Propriedades adicionais =====

    @Test
    @DisplayName("Propriedade: Duas execuções independentes produzem a MESMA saída")
    void main_twice_producesSameOutput() {
        String out1 = runMainAndCapture(new String[0]);
        String out2 = runMainAndCapture(new String[] { "x" });
        assertEquals(out1, out2, "Saídas de execuções independentes devem ser idênticas");
        // E ambas devem conter o conteúdo esperado
        assertExpectedOutput(out1);
        assertExpectedOutput(out2);
    }

    @Test
    @DisplayName("Propriedade: Ordem das linhas é estável (depósito -> transferência -> saldos finais)")
    void main_linesOrder_isStable() {
        String out = runMainAndCapture(null);
        String[] lines = normalizeLines(out);
        // Confirmar ordem exata das 3 primeiras linhas
        assertEquals("Saldo CC: 1000.00", lines[0]);
        assertEquals("Saldo CC após transferência: 700.00", lines[1]);
        assertEquals("Saldo Poupança: 300.00", lines[2]);
    }
}

