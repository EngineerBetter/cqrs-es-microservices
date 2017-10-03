package com.paas.microservices;

public class RepositoryAccountDomainService implements AccountDomainService {
	private AccountRepository repo;

	public RepositoryAccountDomainService(AccountRepository repo) {
		this.repo = repo;
	}

	@Override
	public Account createAccount() {
		Account account = new Account(1, 0d);
		return repo.save(account);
	}

	@Override
	public double getBalance(Integer accountNumber) {
		Account account = repo.load(accountNumber);
		return account.balance;
	}

}
