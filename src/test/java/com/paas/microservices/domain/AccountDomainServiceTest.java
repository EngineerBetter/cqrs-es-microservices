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
import com.paas.microservices.domain.TransactionRow.TransactionType;

public class AccountDomainServiceTest {
	private AccountRepository repo;
	private AccountDomainService accountService;
	private StoringEventBus eventBus;
	private ExceptionEventHandler exceptionHandler;
	private Account account;

	@Before
	public void setup() {
		exceptionHandler = new ExceptionEventHandler();
		eventBus = new StoringEventBus(exceptionHandler);
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

		eventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(50d);

		eventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d));
		balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(100d);
	}

	@Test
	public void creditsAreIdempotent() {
		assertThat(account.balance).isEqualTo(0);

		UUID creditTxId = UUID.randomUUID();
		eventBus.post(new AccountCreditRequestDomainEvent(creditTxId, account.accountNumber, 50d));
		eventBus.post(new AccountCreditRequestDomainEvent(creditTxId, account.accountNumber, 50d));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(50d);
	}

	@Test
	public void accountsAreDebitable() {
		assertThat(account.balance).isEqualTo(0);
		eventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 200d));

		eventBus.post(new AccountDebitRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 100d));
		eventBus.post(new AccountDebitRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(50);
	}

	@Test
	public void debitsAreIdempotent() {
		assertThat(account.balance).isEqualTo(0);

		UUID creditTxID = UUID.randomUUID();
		eventBus.post(new AccountCreditRequestDomainEvent(creditTxID, account.accountNumber, 100d));
		UUID debitTxID = UUID.randomUUID();
		eventBus.post(new AccountDebitRequestDomainEvent(debitTxID, account.accountNumber, 30d));
		eventBus.post(new AccountDebitRequestDomainEvent(debitTxID, account.accountNumber, 30d));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(70d);
	}


	@Test
	public void accountsCannotBeDebitedBelowZero() {
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(0d);
		eventBus.post(new AccountDebitRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 30d));
		exceptionHandler.contains(new RuntimeException("You are trying to debit more than your account balance currently has"));
	}

	@Test
	public void transactionHistoriesAreGettable() {
		assertThat(account.balance).isEqualTo(0);

		eventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 50d));
		eventBus.post(new AccountCreditRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 125d));
		eventBus.post(new AccountDebitRequestDomainEvent(UUID.randomUUID(), account.accountNumber, 30d));
		double balance = accountService.getBalance(account.accountNumber);
		assertThat(balance).isEqualTo(145d);

		TransactionHistory history = accountService.getTransactionHistory(account.accountNumber);
		assertThat(history.transactions.get(0)).isEqualTo(new TransactionRow(TransactionType.CREDIT, 50d, 50d));
		assertThat(history.transactions.get(1)).isEqualTo(new TransactionRow(TransactionType.CREDIT, 125d, 175d));
		assertThat(history.transactions.get(2)).isEqualTo(new TransactionRow(TransactionType.DEBIT, 30d, 145d));
	}
}
