package com.paas.microservices.domain;

import java.util.UUID;

import com.paas.microservices.Event;

public class AccountDebitRequestDomainEvent implements Event {
	public final UUID eventId;
	public final UUID accountNumber;
	public final double amount;

	public AccountDebitRequestDomainEvent(UUID eventId, UUID accountNumber, double amount) {
		super();
		this.eventId = eventId;
		this.accountNumber = accountNumber;
		this.amount = amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		AccountDebitRequestDomainEvent other = (AccountDebitRequestDomainEvent) obj;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
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
		return "AccountDebitRequestDomainEvent [eventId=" + eventId + ", accountNumber=" + accountNumber + ", amount="
				+ amount + "]";
	}
}
