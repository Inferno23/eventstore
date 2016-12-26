package io.github.burns.eventstore.impl;

import io.github.burns.eventstore.Event;
import rx.functions.Action1;

import java.util.function.Predicate;

import static io.github.burns.eventstore.EventStore.INITIAL_EVENT_ID;

/**
 * This class is responsible for processing events
 * and making sure that it didn't miss any events.
 *
 * @param <T> The type associated with events.
 * @param <S> The scope of who should see an event.
 * @param <C> The type of context.
 */
public class EventProcessor<T, S, C> {
  private final EventStoreImpl<T, S, C> eventStore;

  /**
   * Initialize the EventProcessor.  Registers an EventStore
   * and processes all events that are received.
   *
   * @param scopePredicate A predicate for the scope of events to handle.
   * @param eventProcessingConsumer Function to process an event received.
   */
  public EventProcessor(Predicate<S> scopePredicate,
                        Action1<Event<T, S, C>> eventProcessingConsumer) {
    eventStore = EventStoreFactory.getInstance();
    eventStore.register(scopePredicate)
        .subscribe(eventProcessingConsumer,
            System.err::println);
    eventStore.getEvents(INITIAL_EVENT_ID);
  }
}
