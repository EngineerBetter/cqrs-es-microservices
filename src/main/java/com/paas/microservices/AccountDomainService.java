package com.paas.microservices;

import java.util.UUID;

public interface AccountDomainService {

	Account createAccount(UUID transactionId);

	void creditAccount(UUID transactionId, UUID accountNumber, double amount);

	double getBalance(UUID accountNumber);

	void debitAccount(UUID debitTxId, UUID accountNumber, double amountToBeDebited);
}
