package com.paas.microservices;

public interface AccountRepository {
	Integer create(AccountCreateRequestEvent event);

	Account save(AccountUpdateRequestEvent event);

	Account load(Integer accountNumber);

	Integer getTotalNumberOfAccounts();
}
