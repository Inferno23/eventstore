package io.github.burns.eventstore;

import rx.Observable;

import java.util.function.Predicate;

/**
 * The interface for an event store, which is able
 * to publish events to subscribers.  It also is
 * able to replay events in case a subscriber missed
 * a prior event.
 *
 * @param <T> The type associated with events.
 * @param <S> The scope of who should see an event.
 */
public interface EventStore<T, S, C> {

  /**
   * The initial event ID to use.
   */
  int INITIAL_EVENT_ID = 1;

  /**
   * Publish an event to all subscribers of the EventStore.
   *
   * @param type  The type of event that is being published.
   * @param scope The scope of who should see the published event.
   */
  default void publishEvent(T type, S scope) {
    publishEvent(type, scope, null);
  }

  /**
   * Publish an event to all subscribers of the EventStore.
   *
   * @param type    The type of event that is being published.
   * @param scope   The scope of who should see the published event.
   * @param content The content to include with the event.
   */
  void publishEvent(T type, S scope, C content);

  /**
   * Get the id of the latest Event to have been published.
   *
   * @return The id of the latest Event to have been published.
   */
  int latestEventId();

  /**
   * Register to receive Events.
   *
   * @param publishingPredicate A predicate which determines if
   *                            which scopes of events should
   *                            be published to the registerer.
   * @return An Observable which will emit the Events.
   */
  Observable<Event<T, S, C>> register(Predicate<S> publishingPredicate);

  /**
   * Replay events starting with the given event ID.
   *
   * @param eventId The event ID to start replaying with.
   */
  void getEvents(int eventId);
}
