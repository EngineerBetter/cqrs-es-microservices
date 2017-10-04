package com.paas.microservices;

import java.util.UUID;

public class AccountCreatedEvent implements Event {
	public final UUID parentTransactionId;
	public final UUID createdId;


	public AccountCreatedEvent(UUID parentTransactionId, UUID createdId) {
		this.parentTransactionId = parentTransactionId;
		this.createdId = createdId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdId == null) ? 0 : createdId.hashCode());
		result = prime * result + ((parentTransactionId == null) ? 0 : parentTransactionId.hashCode());
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
		if (createdId == null) {
			if (other.createdId != null)
				return false;
		} else if (!createdId.equals(other.createdId))
			return false;
		if (parentTransactionId == null) {
			if (other.parentTransactionId != null)
				return false;
		} else if (!parentTransactionId.equals(other.parentTransactionId))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "AccountCreatedEvent [parentTransactionId=" + parentTransactionId + ", createdId=" + createdId + "]";
	}
}
