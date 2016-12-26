package io.github.burns.eventstore.impl;

import io.github.burns.eventstore.Event;
import io.github.burns.eventstore.EventStore;
import org.apache.commons.lang3.tuple.Pair;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Implementation of the EventStore interface.
 *
 * @param <T> The type associated with events.
 * @param <S> The scope of who should see an event.
 * @param <C> The type of context.
 */
public class EventStoreImpl<T, S, C> implements EventStore<T, S, C> {
  private final List<Pair<ReplaySubject<Event<T, S, C>>, Predicate<S>>> tupleList;
  private final AtomicInteger idCounter;
  private final List<Event<T, S, C>> eventList;

  /**
   * Initialize the EventStore so users can register for events
   * and publish new events.
   */
  EventStoreImpl() {
    idCounter = new AtomicInteger(INITIAL_EVENT_ID);
    tupleList = new ArrayList<>();
    eventList = new ArrayList<>();
  }

  @Override
  public void publishEvent(T type, S scope, C content) {
    final Event<T, S, C> event = new Event<>(idCounter.getAndIncrement(), type, scope, content);
    eventList.add(event);
    tupleList.forEach(publishEventToTuple(event));
  }

  @Override
  public int latestEventId() {
    return idCounter.get() - 1;
  }

  @Override
  public Observable<Event<T, S, C>> register(Predicate<S> publishingPredicate) {
    final ReplaySubject<Event<T, S, C>> subject = ReplaySubject.create();
    tupleList.add(Pair.of(subject, publishingPredicate));
    return subject.asObservable();
  }

  @Override
  public void getEvents(int eventId) {
    eventList.stream()
        .filter(event -> event.id >= eventId)
        .forEach(event -> tupleList.forEach(publishEventToTuple(event)));
  }

  /**
   * Helper to produce a consumer which handles publishing an
   * event to a subject that is held in a tuple.
   *
   * @param event The event to publish.
   * @return A consumer to pass into a forEach for the tuple list.
   */
  private Consumer<Pair<ReplaySubject<Event<T, S, C>>, Predicate<S>>> publishEventToTuple(
      Event<T, S, C> event) {
    return tuple -> {
      final ReplaySubject<Event<T, S, C>> subject = tuple.getLeft();
      final Predicate<S> predicate = tuple.getRight();

      subject.onNext(predicate.test(event.scope)
          ? event : new Event<>(event.id, event.type, event.scope, null));
    };
  }
}
