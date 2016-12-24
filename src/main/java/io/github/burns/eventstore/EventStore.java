package io.github.burns.eventstore;

import rx.Observable;

/**
 * The interface for an event store, which is able
 * to publish events to subscribers.  It also is
 * able to replay events in case a subscriber missed
 * a prior event.
 *
 * @param <T> The type associated with events.
 */
public interface EventStore<T> {

  /**
   * Publish an event to all subscribers of the EventStore.
   *
   * @param event The event to publish.
   */
  void publishEvent(Event<T> event);

  /**
   * Get the id of the latest Event to have been published.
   *
   * @return The id of the latest Event to have been published.
   */
  int latestEventId();

  /**
   * Register to receive Events.
   *
   * @return An Observable which will emit the Events.
   */
  Observable<Event<T>> register();
}
