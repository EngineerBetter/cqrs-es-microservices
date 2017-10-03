package com.paas.microservices;

import java.util.UUID;

public interface Event {
	UUID getTransactionId();
}
