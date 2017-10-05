package com.paas.microservices.data;

import java.util.UUID;

import com.paas.microservices.CausableEvent;
import com.paas.microservices.Event;

public class AccountCreateRequestDataEvent implements CausableEvent {
	public final UUID eventId;
	public final double startingBalance;
	public final Event cause;

	public AccountCreateRequestDataEvent(UUID eventId, double startingBalance, Event cause) {
		this.eventId = eventId;
		this.startingBalance = startingBalance;
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
		result = prime * result + ((cause == null) ? 0 : cause.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		long temp;
		temp = Double.doubleToLongBits(startingBalance);
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
		AccountCreateRequestDataEvent other = (AccountCreateRequestDataEvent) obj;
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
		if (Double.doubleToLongBits(startingBalance) != Double.doubleToLongBits(other.startingBalance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountCreateRequestDataEvent [eventId=" + eventId + ", startingBalance=" + startingBalance + ", cause="
				+ cause + "]";
	}
}
