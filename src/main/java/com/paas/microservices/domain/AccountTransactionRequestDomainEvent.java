package com.paas.microservices.domain;

import java.util.UUID;

import com.paas.microservices.CausableEvent;
import com.paas.microservices.Event;

public class AccountTransactionRequestDomainEvent implements CausableEvent {
	public final UUID eventId;
	public final UUID accountNumber;
	public final double amount;
	public final TransactionType type;
	public final Event cause;

	public AccountTransactionRequestDomainEvent(UUID eventId, UUID accountNumber, double amount,TransactionType type, Event cause) {
		this.eventId = eventId;
		this.accountNumber = accountNumber;
		this.amount = amount;
		this.type = type;
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
		result = prime * result + ((accountNumber == null) ? 0 : accountNumber.hashCode());
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((cause == null) ? 0 : cause.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		AccountTransactionRequestDomainEvent other = (AccountTransactionRequestDomainEvent) obj;
		if (accountNumber == null) {
			if (other.accountNumber != null)
				return false;
		} else if (!accountNumber.equals(other.accountNumber))
			return false;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
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
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountTransactionRequestDomainEvent [eventId=" + eventId + ", accountNumber=" + accountNumber
				+ ", amount=" + amount + ", type=" + type + ", cause=" + cause + "]";
	}
}
