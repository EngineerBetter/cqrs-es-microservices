package com.paas.microservices.domain;

import java.util.UUID;

import com.paas.microservices.Account;
import com.paas.microservices.CausableEvent;
import com.paas.microservices.Event;

public class AccountCreatedDomainEvent implements CausableEvent {
	public final UUID causeEventId;
	public final Account account;
	public final Event cause;

	public AccountCreatedDomainEvent(UUID causeEventId, Account account, Event cause) {
		this.causeEventId = causeEventId;
		this.account = account;
		this.cause = cause;
	}

	@Override
	public Event getCause() {
		return cause;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((cause == null) ? 0 : cause.hashCode());
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
		if (cause == null) {
			if (other.cause != null)
				return false;
		} else if (!cause.equals(other.cause))
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
		return "AccountCreatedDomainEvent [causeEventId=" + causeEventId + ", account=" + account + ", cause=" + cause
				+ "]";
	}
}
