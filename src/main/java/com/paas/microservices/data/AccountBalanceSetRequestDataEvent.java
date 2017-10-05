package com.paas.microservices.data;

import java.util.UUID;

import com.paas.microservices.Account;
import com.paas.microservices.CausableEvent;
import com.paas.microservices.Event;

public class AccountBalanceSetRequestDataEvent implements CausableEvent {
	public final UUID eventId;
	public final Account account;
	public final Event cause;


	public AccountBalanceSetRequestDataEvent(UUID eventId, Account account, Event cause) {
		this.eventId = eventId;
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
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
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
		AccountBalanceSetRequestDataEvent other = (AccountBalanceSetRequestDataEvent) obj;
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
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "AccountBalanceSetRequestDataEvent [eventId=" + eventId + ", account=" + account + ", cause=" + cause
				+ "]";
	}
}
