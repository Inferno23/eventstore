package io.github.burns.eventstore.impl;

import io.github.burns.eventstore.Event;
import io.github.burns.eventstore.EventStore;
import rx.AsyncEmitter;
import rx.Observable;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import rx.subjects.ReplaySubject;

/**
 * Created by mike on 12/23/16.
 */
public class EventStoreImpl<T> implements EventStore<T> {

  private ReplaySubject<Event<T>> subject;

  public EventStoreImpl() {
    subject = ReplaySubject.create();
  }

  @Override
  public void publishEvent(Event event) {
    subject.onNext(event);
  }

  /**
   * Get the id of the latest Event to have been published.
   *
   * @return The id of the latest Event to have been published.
   */
  @Override
  public int latestEventId() {
    return 0;
  }

  /**
   * Register to receive Events.
   *
   * @return An Observable which will emit the Events.
   */
  @Override
  public Observable<Event<T>> register() {
    return subject.asObservable();
  }
}
