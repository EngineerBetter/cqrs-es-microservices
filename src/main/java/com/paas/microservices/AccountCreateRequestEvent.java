package com.paas.microservices;

import java.util.UUID;

public class AccountCreateRequestEvent implements Event {
	public final UUID transactionId;
	public final double startingBalance;

	public AccountCreateRequestEvent(UUID transactionId, double startingBalance) {
		this.transactionId = transactionId;
		this.startingBalance = startingBalance;
	}

	@Override
	public UUID getTransactionId() {
		return transactionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(startingBalance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		AccountCreateRequestEvent other = (AccountCreateRequestEvent) obj;
		if (Double.doubleToLongBits(startingBalance) != Double.doubleToLongBits(other.startingBalance))
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
		return "AccountCreateRequestEvent [transactionId=" + transactionId + ", startingBalance=" + startingBalance
				+ "]";
	}


}
