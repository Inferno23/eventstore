package io.github.burns.eventstore.impl;

import io.github.burns.eventstore.Event;
import io.github.burns.eventstore.EventStore;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Implementation of the EventStore interface.
 */
public class EventStoreImpl<T, S> implements EventStore<T, S> {

  private final Map<ReplaySubject<Event<T, S>>, Predicate<S>> subjectPredicateMap;
  private final AtomicInteger idCounter;

  /**
   * Initialize the EventStore so users can register for events
   * and publish new events.
   */
  public EventStoreImpl() {
    idCounter = new AtomicInteger(1);
    subjectPredicateMap = new HashMap<>();
  }

  @Override
  public void publishEvent(T type, S scope) {
    final Event<T, S> event = new Event<>(idCounter.getAndIncrement(), type, scope);
    subjectPredicateMap.entrySet().forEach(entry -> {
          if (entry.getValue().test(scope)){
            entry.getKey().onNext(event);
          }
        }
    );
  }

  @Override
  public int latestEventId() {
    return idCounter.get() - 1;
  }

  @Override
  public Observable<Event<T, S>> register(Predicate<S> publishingPredicate) {
    final ReplaySubject<Event<T, S>> subject = ReplaySubject.create();
    subjectPredicateMap.put(subject, publishingPredicate);
    return subject.asObservable();
  }
}
