package com.paas.microservices;

public interface CausableEvent extends Event {
	Event getCause();
}
