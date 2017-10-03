package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AccountTest {
	@Test
	public void accountsAreSemanticallyEqual() {
		Account account1 = new Account(123, 123.12);
		Account account2 = new Account(123, 123.12);
		assertThat(account1).isEqualTo(account2);
	}
}
