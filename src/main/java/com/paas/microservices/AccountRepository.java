package com.paas.microservices;

public interface AccountRepository {
	Account save(AccountSaveRequestEvent event);

	Account load(Integer accountNumber);
}
