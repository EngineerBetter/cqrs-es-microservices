package com.paas.microservices.domain;

import java.util.UUID;

public interface AccountDomainService {

	void createAccount(AccountCreateRequestDomainEvent event);

	void creditAccount(AccountTransactionRequestDomainEvent event);

	double getBalance(UUID accountNumber);

	TransactionHistory getTransactionHistory(UUID accountNumber);
}
