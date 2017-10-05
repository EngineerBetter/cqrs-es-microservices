package com.paas.microservices.domain;

import java.util.UUID;

public interface AccountDomainService {

	void createAccount(AccountCreateRequestDomainEvent event);

	void creditAccount(UUID transactionId, UUID accountNumber, double amount);

	double getBalance(UUID accountNumber);

	void debitAccount(UUID debitTxId, UUID accountNumber, double amountToBeDebited);

	TransactionHistory getTransactionHistory(UUID accountNumber);
}
