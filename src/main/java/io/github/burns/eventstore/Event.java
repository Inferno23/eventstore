package io.github.burns.eventstore;

/**
 * Created by mike on 12/23/16.
 * @param <T> The type of the event.
 */
public class Event<T> {
  /**
   * The id number of the event.
   */
  public final int id;

  /**
   * The type of event.
   */
  public final T type;

  /**
   * Construct an Event.
   *
   * @param id The id number of the event.
   * @param type The type of event.
   */
  public Event(int id, T type) {
    this.id = id;
    this.type = type;
  }
}
