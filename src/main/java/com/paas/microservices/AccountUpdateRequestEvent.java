package com.paas.microservices;

import java.util.UUID;

public class AccountUpdateRequestEvent implements Event {
	public final UUID transactionId;
	public final Account account;

	public AccountUpdateRequestEvent(UUID transactionId, Account account) {
		this.transactionId = transactionId;
		this.account = account;
	}

	@Override
	public UUID getTransactionId() {
		return transactionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((transactionId == null) ? 0 : transactionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccountUpdateRequestEvent other = (AccountUpdateRequestEvent) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (transactionId == null) {
			if (other.transactionId != null)
				return false;
		} else if (!transactionId.equals(other.transactionId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountUpdateRequestEvent [transactionId=" + transactionId + ", account=" + account + "]";
	}
}
