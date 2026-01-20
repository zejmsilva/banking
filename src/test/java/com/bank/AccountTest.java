package com.bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    @Test
    void testBalance() {
        Account acc = new Account(100.0);
        assertEquals(100.0, acc.getBalance());
    }
}
