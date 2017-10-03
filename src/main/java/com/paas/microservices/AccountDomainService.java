package com.paas.microservices;

public interface AccountDomainService {

	Account createAccount();

	double getBalance(Integer accountNumber);
}
