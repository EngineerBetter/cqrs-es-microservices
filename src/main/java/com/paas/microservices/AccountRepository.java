package com.paas.microservices;

import java.util.UUID;

public interface AccountRepository {
	void create(AccountCreateRequestEvent event);

	Account save(AccountUpdateRequestEvent event);

	Account load(UUID accountNumber);

	Integer getTotalNumberOfAccounts();
}
