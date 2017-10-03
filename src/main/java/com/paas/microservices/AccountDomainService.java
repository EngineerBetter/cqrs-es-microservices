package com.paas.microservices;

import java.util.UUID;

public interface AccountDomainService {

	Account createAccount(UUID transactionId);

	void creditAccount(UUID transactionId, Integer accountNumber, double amount);

	double getBalance(Integer accountNumber);

	void debitAccount(UUID debitTxId, Integer accountNumber, double amountToBeDebited);
}
