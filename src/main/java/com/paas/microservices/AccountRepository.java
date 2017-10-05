package com.paas.microservices;

import java.util.UUID;

public interface AccountRepository {
	void create(AccountCreateRequestDataEvent event);

	Account save(AccountBalanceSetRequestDataEvent event);

	Account load(UUID accountNumber);

	Integer getTotalNumberOfAccounts();
}
