# cqrs-es-microservices

## Summary

* Transactions prevent bad things from happening.
* Eventual consistency requires measures to correct bad things that will inevitably happen.

## Event-Driven Architecture

* Write operations should be separate to read operations
* Read operations should not have side-effects
* Write operations should be idempotent
* Write operations should be deterministic
* Write operations should be effected by events, delivered by a message bus
* Write operations should not return values; they should emit "response" events
* Invokers of write operations should listen for a "response" event, and then return their own, appropriate to their level of abstraction
* Anything that the invoker of a write operation needs to return in a response must be send down in the write request event, so that the handler can pass the desired information back.
* Events must be immutable, and implement hashCode and equals
* Services should maintain an in-memory view of state, built up as each event is received

## Event Sourcing

* Events should be stored.
* Event stores should be merged to achieve consistency.
* Events should be replayed after a merge. Services should reset their internal in-memory views of state before this replay, allowing them to rebuild their view.
* Failing events should be marked as such, and should not be replayed.
* Events whose cause failed should also be marked failed, and should not be replayed.

## Eventual Consistency

* All of the stuff above is _mandatory_!
* Events must maintain causal history of an 'aggregate' or domain object. For instance, all transactions pertaining to an Account must refer back to a prior known state of that Account. If this does not happen, we cannot order events.
* When a diverging history is detected (two events with the same cause) then a context-specific handling mechanism needs to be invoked. Do we reject both branches? Do we play both? Do we merge them? Do we perform a compensating transaction? These all depend on use case.
