package com.paas.microservices.data;

import java.util.UUID;

import com.paas.microservices.Event;

public class AccountCreatedDataEvent implements Event {
	public final UUID parentEventId;
	public final UUID createdId;


	public AccountCreatedDataEvent(UUID parentEventId, UUID createdId) {
		this.parentEventId = parentEventId;
		this.createdId = createdId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdId == null) ? 0 : createdId.hashCode());
		result = prime * result + ((parentEventId == null) ? 0 : parentEventId.hashCode());
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
		AccountCreatedDataEvent other = (AccountCreatedDataEvent) obj;
		if (createdId == null) {
			if (other.createdId != null)
				return false;
		} else if (!createdId.equals(other.createdId))
			return false;
		if (parentEventId == null) {
			if (other.parentEventId != null)
				return false;
		} else if (!parentEventId.equals(other.parentEventId))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "AccountCreatedEvent [parentEventId=" + parentEventId + ", createdId=" + createdId + "]";
	}
}
