package com.paas.microservices;

import java.util.UUID;

public class AccountDebitedDomainEvent implements Event {
	public final UUID eventId;
	public final UUID accountNumber;
	public final double amount;
	public final double resultingBalance;

	public AccountDebitedDomainEvent(UUID eventId, UUID accountNumber, double amount, double resultingBalance) {
		this.eventId = eventId;
		this.accountNumber = accountNumber;
		this.amount = amount;
		this.resultingBalance = resultingBalance;
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
		temp = Double.doubleToLongBits(resultingBalance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		AccountDebitedDomainEvent other = (AccountDebitedDomainEvent) obj;
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
		if (Double.doubleToLongBits(resultingBalance) != Double.doubleToLongBits(other.resultingBalance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountCreditedEvent [eventId=" + eventId + ", accountNumber=" + accountNumber + ", amount=" + amount
				+ ", resultingBalance=" + resultingBalance + "]";
	}
}
