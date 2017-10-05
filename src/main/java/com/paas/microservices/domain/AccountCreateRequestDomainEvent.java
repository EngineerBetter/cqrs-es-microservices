package com.paas.microservices.domain;

import java.util.UUID;

import com.paas.microservices.Event;

public class AccountCreateRequestDomainEvent implements Event {
	public final UUID eventId;

	public AccountCreateRequestDomainEvent(UUID eventId) {
		this.eventId = eventId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		AccountCreateRequestDomainEvent other = (AccountCreateRequestDomainEvent) obj;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountCreateRequestDomainEvent [eventId=" + eventId + "]";
	}
}
