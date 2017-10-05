package com.paas.microservices.data;

import java.util.UUID;

import com.paas.microservices.Account;

public interface AccountRepository {
	void create(AccountCreateRequestDataEvent event);

	Account save(AccountBalanceSetRequestDataEvent event);

	Account load(UUID accountNumber);

	Integer getTotalNumberOfAccounts();
}
