package com.paas.microservices.domain;

import java.util.HashSet;
import java.util.Set;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

public class ExceptionEventHandler implements SubscriberExceptionHandler {

	private Set<Throwable> exceptionsThrown = new HashSet<>();

	@Override
	public void handleException(Throwable exception, SubscriberExceptionContext context) {
		exceptionsThrown.add(exception);
	}

	public boolean contains(Throwable exception) {
		return exceptionsThrown.contains(exception);
	}

}
