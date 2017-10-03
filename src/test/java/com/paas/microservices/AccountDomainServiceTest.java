package com.paas.microservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class AccountDomainServiceTest {
	private AccountRepository repo;
	private AccountDomainService accountService;

	@Before
	public void setup() {
		repo = new InMemoryAccountRepository();
		accountService = new RepositoryAccountDomainService(repo);
	}

	@Test
	public void newAccountsHaveAZeroBalance() {
		UUID transactionId = UUID.randomUUID();
		Account account = accountService.createAccount(transactionId);
		assertThat(account.balance).isEqualTo(0d);
	}

	@Test
	public void createAccountRequestsAreIdempotent() {
		UUID transactionId = UUID.randomUUID();
		accountService.createAccount(transactionId);
		accountService.createAccount(transactionId);
		assertThat(repo.getTotalNumberOfAccounts()).isEqualTo(1);
	}

	@Test
	public void accountsAreCreditable() {
		UUID transactionId = UUID.randomUUID();
		Account created = accountService.createAccount(transactionId);
		assertThat(created.balance).isEqualTo(0);

		UUID creditTxId = UUID.randomUUID();
		accountService.creditAccount(creditTxId, created.accountNumber, 50d);
		double balance = accountService.getBalance(created.accountNumber);
		assertThat(balance).isEqualTo(50d);
	}
}
