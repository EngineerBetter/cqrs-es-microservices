package com.paas.microservices;

import java.util.UUID;

public class AccountCreatedEvent implements Event {
	public final UUID transactionId;
	public final UUID parentTransactionId;
	public final int createdId;


	public AccountCreatedEvent(UUID transactionId, UUID parentTransactionId, int createdId) {
		this.transactionId = transactionId;
		this.parentTransactionId = parentTransactionId;
		this.createdId = createdId;
	}

	@Override
	public UUID getTransactionId() {
		return transactionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + createdId;
		result = prime * result + ((parentTransactionId == null) ? 0 : parentTransactionId.hashCode());
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
		AccountCreatedEvent other = (AccountCreatedEvent) obj;
		if (createdId != other.createdId)
			return false;
		if (parentTransactionId == null) {
			if (other.parentTransactionId != null)
				return false;
		} else if (!parentTransactionId.equals(other.parentTransactionId))
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
		return "AccountCreatedEvent [transactionId=" + transactionId + ", parentTransactionId=" + parentTransactionId
				+ ", createdId=" + createdId + "]";
	}
}
