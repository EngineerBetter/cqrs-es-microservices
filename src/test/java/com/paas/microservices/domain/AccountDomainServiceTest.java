package com.paas.microservices.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.paas.microservices.Account;
import com.paas.microservices.AccountGetter;
import com.paas.microservices.StoringEventBus;
import com.paas.microservices.data.AccountRepository;
import com.paas.microservices.data.InMemoryAccountRepository;

public class AccountDomainServiceTest {
	private AccountRepository repo;
	private AccountDomainService accountService;
	private StoringEventBus eventBus;
	private Account account;

	@Before
	public void setup() {
		eventBus = new StoringEventBus();
		repo = new InMemoryAccountRepository(eventBus);
		accountService = new RepositoryAccountDomainService(repo, eventBus);
		createAccount();
	}


	private void createAccount() {
		AccountGetter getter = new AccountGetter();
		eventBus.register(getter);
		AccountCreateRequestDomainEvent event = new AccountCreateRequestDomainEvent(UUID.randomUUID());
		eventBus.post(event);
		account = getter.getAccount();
	}

	@Test
	public void newAccountsHaveAZeroBalance() {
		assertThat(this.account.balance).isEqualTo(0d);
	}

	@Test
	public void createAccountRequestsAreIdempotent() {
		AccountCreateRequestDomainEvent event = new AccountCreateRequestDomainEvent(UUID.randomUUID());
		int accountsBefore = repo.getTotalNumberOfAccounts();
		eventBus.post(event);
		eventBus.post(event);
		assertThat(repo.getTotalNumberOfAccounts()).isEqualTo(accountsBefore + 1);
	}

	@Test
	public void accountsAreCreditable() {
		assertThat(account.balance).isEqualTo(0);

		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d, TransactionType.CREDIT));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(50d);

		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d, TransactionType.CREDIT));
		balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(100d);
	}

	@Test
	public void creditsAreIdempotent() {
		assertThat(account.balance).isEqualTo(0);

		UUID creditTxId = UUID.randomUUID();
		eventBus.post(new AccountTransactionRequestDomainEvent(creditTxId, account.accountNumber, 50d, TransactionType.CREDIT));
		eventBus.post(new AccountTransactionRequestDomainEvent(creditTxId, account.accountNumber, 50d, TransactionType.CREDIT));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(50d);
	}

	@Test
	public void accountsAreDebitable() {
		assertThat(account.balance).isEqualTo(0);
		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 200d, TransactionType.CREDIT));

		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 100d, TransactionType.DEBIT));
		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d, TransactionType.DEBIT));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(50);
	}

	@Test
	public void debitsAreIdempotent() {
		assertThat(account.balance).isEqualTo(0);

		UUID creditTxID = UUID.randomUUID();
		eventBus.post(new AccountTransactionRequestDomainEvent(creditTxID, account.accountNumber, 100d, TransactionType.CREDIT));
		UUID debitTxID = UUID.randomUUID();
		eventBus.post(new AccountTransactionRequestDomainEvent(debitTxID, account.accountNumber, 30d, TransactionType.DEBIT));
		eventBus.post(new AccountTransactionRequestDomainEvent(debitTxID, account.accountNumber, 30d, TransactionType.DEBIT));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(70d);
	}


	@Test
	public void accountsCannotBeDebitedBelowZero() {
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(0d);
		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 30d, TransactionType.DEBIT));
		RuntimeException expectedException = new RuntimeException("You are trying to debit more than your account balance currently has");
		assertThat(eventBus.exceptionThrownMatching(expectedException)).isTrue();
	}

	@Test
	public void transactionHistoriesAreGettable() {
		assertThat(account.balance).isEqualTo(0);

		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d, TransactionType.CREDIT));
		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 125d, TransactionType.CREDIT));
		eventBus.post(new AccountTransactionRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 30d, TransactionType.DEBIT));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(145d);

		TransactionHistory history = accountService.getTransactionHistory(account.accountNumber);
		assertThat(history.transactions.get(0)).isEqualTo(new TransactionRow(TransactionType.CREDIT, 50d, 50d));
		assertThat(history.transactions.get(1)).isEqualTo(new TransactionRow(TransactionType.CREDIT, 125d, 175d));
		assertThat(history.transactions.get(2)).isEqualTo(new TransactionRow(TransactionType.DEBIT, 30d, 145d));
	}
}
