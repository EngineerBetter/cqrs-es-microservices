package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;

public class AccountTest {
	@Test
	public void accountsAreSemanticallyEqual() {
		UUID accountNumber = UUID.randomUUID();
		Account account1 = new Account(accountNumber, 123.12);
		Account account2 = new Account(accountNumber, 123.12);
		assertThat(account1).isEqualTo(account2);
	}
}
