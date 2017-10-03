package com.paas.microservices;

public interface AccountRepository {
	Account save(Account account);

	Account load(Integer accountNumber);
}
