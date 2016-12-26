package io.github.burns.eventstore.impl;

import static java.util.Optional.ofNullable;

/**
 * Factory to produce an instance of an EventStore.
 */
public class EventStoreFactory {
  private static EventStoreImpl instance;

  /**
   * Get an instance of an EventStore.
   *
   * @param <T> The type associated with events.
   * @param <S> The scope of who should see an event.
   * @param <C> The type of context.
   * @return The instance of an EventStore.
   */
  public static <T, S, C> EventStoreImpl<T, S, C> getInstance() {
    instance = ofNullable(instance).orElseGet(EventStoreImpl::new);
    return instance;
  }

  /**
   * Close the instance of the EventStore.
   */
  public static void closeInstance() {
    instance = null;
  }
}
