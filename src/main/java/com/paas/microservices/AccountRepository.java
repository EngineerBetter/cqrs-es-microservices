package com.paas.microservices;

import java.util.UUID;

public interface AccountRepository {
	UUID create(AccountCreateRequestEvent event);

	Account save(AccountUpdateRequestEvent event);

	Account load(UUID accountNumber);

	Integer getTotalNumberOfAccounts();

	TransactionHistory getTransactionHistory(UUID accountNumber);
}
