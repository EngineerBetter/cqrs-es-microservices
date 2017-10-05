package com.paas.microservices.data;

import java.util.UUID;

import com.paas.microservices.Account;
import com.paas.microservices.Event;

public class AccountBalanceSetRequestDataEvent implements Event {
	public final UUID eventId;
	public final Account account;


	public AccountBalanceSetRequestDataEvent(UUID eventId, Account account) {
		this.eventId = eventId;
		this.account = account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account.accountNumber == null) ? 0 : account.accountNumber.hashCode());
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
		} else if (!account.accountNumber.equals(other.account.accountNumber))
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
		return "AccountUpdateRequestEvent [eventId=" + eventId + ", account=" + account + "]";
	}
}