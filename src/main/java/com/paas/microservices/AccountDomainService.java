package com.paas.microservices;

import java.util.UUID;

public interface AccountDomainService {

	Account createAccount(UUID transactionId);

	double getBalance(Integer accountNumber);
}
