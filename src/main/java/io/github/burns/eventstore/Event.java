package io.github.burns.eventstore;

/**
 * An event that has an ID number and a type.
 *
 * @param <T> The type of the event.
 * @param <S> The scope of the event.
 */
public class Event<T, S> {
  /**
   * The id number of the event.
   */
  public final int id;

  /**
   * The type of event.
   */
  public final T type;

  /**
   * The scope of the event.
   */
  public final S scope;

  /**
   * Construct an Event.
   *  @param id The id number of the event.
   * @param type The type of event.
   * @param scope The scope of the event.
   */
  public Event(int id, T type, S scope) {
    this.id = id;
    this.type = type;
    this.scope = scope;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", type=" + type +
        ", scope=" + scope +
        '}';
  }
}
