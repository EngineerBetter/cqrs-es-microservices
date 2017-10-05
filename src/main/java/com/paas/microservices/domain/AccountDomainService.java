package com.paas.microservices.domain;

import java.util.UUID;

public interface AccountDomainService {

	void createAccount(AccountCreateRequestDomainEvent event);

	void creditAccount(AccountCreditRequestDomainEvent event);

	double getBalance(UUID accountNumber);

	void debitAccount(UUID debitTxId, UUID accountNumber, double amountToBeDebited);

	TransactionHistory getTransactionHistory(UUID accountNumber);
}
