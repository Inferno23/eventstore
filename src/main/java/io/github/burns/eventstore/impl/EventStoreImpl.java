package io.github.burns.eventstore.impl;

import io.github.burns.eventstore.Event;
import io.github.burns.eventstore.EventStore;
import rx.Observable;

/**
 * Created by mike on 12/23/16.
 */
public class EventStoreImpl implements EventStore {

  @Override
  public Observable<Void> publishEvent(Event event) {
    return Observable.just(null);
  }
}
