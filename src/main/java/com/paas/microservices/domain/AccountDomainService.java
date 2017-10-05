package com.paas.microservices.domain;

import java.util.UUID;

public interface AccountDomainService {

	void createAccount(AccountCreateRequestDomainEvent event);

	void creditAccount(AccountCreditRequestDomainEvent event);

	void debitAccount(AccountDebitRequestDomainEvent event);

	double getBalance(UUID accountNumber);

	TransactionHistory getTransactionHistory(UUID accountNumber);
}
