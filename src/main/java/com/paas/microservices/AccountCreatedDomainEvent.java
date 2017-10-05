package com.paas.microservices;

import java.util.UUID;

public class AccountCreatedDomainEvent implements Event {
	public final UUID causeEventId;
	public final Account account;

	public AccountCreatedDomainEvent(UUID causeEventId, Account account) {
		super();
		this.causeEventId = causeEventId;
		this.account = account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((causeEventId == null) ? 0 : causeEventId.hashCode());
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
		AccountCreatedDomainEvent other = (AccountCreatedDomainEvent) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (causeEventId == null) {
			if (other.causeEventId != null)
				return false;
		} else if (!causeEventId.equals(other.causeEventId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountCreatedDomainEvent [causeEventId=" + causeEventId + ", account=" + account + "]";
	}
}
