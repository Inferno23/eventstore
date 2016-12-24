package io.github.burns.eventstore.impl;

import io.github.burns.eventstore.Event;
import io.github.burns.eventstore.EventStore;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of the EventStore interface.
 */
public class EventStoreImpl<T, S> implements EventStore<T, S> {

  private final ReplaySubject<Event<T, S>> subject;
  private final AtomicInteger idCounter;

  public EventStoreImpl() {
    subject = ReplaySubject.create();
    idCounter = new AtomicInteger(1);
  }

  @Override
  public void publishEvent(T type, S scope) {
    subject.onNext(new Event<>(idCounter.getAndIncrement(), type, scope));
  }

  /**
   * Get the id of the latest Event to have been published.
   *
   * @return The id of the latest Event to have been published.
   */
  @Override
  public int latestEventId() {
    return idCounter.get() - 1;
  }

  /**
   * Register to receive Events.
   *
   * @return An Observable which will emit the Events.
   */
  @Override
  public Observable<Event<T, S>> register() {
    return subject.asObservable();
  }
}
