package com.paas.microservices.data;

import com.paas.microservices.CausableEvent;
import com.paas.microservices.Event;

public class AccountUpdatedDataEvent implements CausableEvent {
	public final AccountBalanceSetRequestDataEvent cause;

	public AccountUpdatedDataEvent(AccountBalanceSetRequestDataEvent cause) {
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
		AccountUpdatedDataEvent other = (AccountUpdatedDataEvent) obj;
		if (cause == null) {
			if (other.cause != null)
				return false;
		} else if (!cause.equals(other.cause))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountUpdatedDataEvent [cause=" + cause + "]";
	}
}
