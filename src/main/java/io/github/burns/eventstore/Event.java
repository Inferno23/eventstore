package io.github.burns.eventstore;

/**
 * An event that has an ID number and a type.
 *
 * @param <T> The type of the event.
 * @param <S> The scope of the event.
 * @param <C> The content type of the event.
 */
public class Event<T, S, C> {
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
   * The content of the event.
   */
  public final C content;

  /**
   * Construct an Event.
   *
   * @param id The id number of the event.
   * @param type The type of event.
   * @param scope The scope of the event.
   * @param content The content associated with the event.
   */
  public Event(int id, T type, S scope, C content) {
    this.id = id;
    this.type = type;
    this.scope = scope;
    this.content = content;
  }

  @Override
  public String toString() {
    return "Event{" +
        "id=" + id +
        ", type=" + type +
        ", scope=" + scope +
        ", content=" + content +
        '}';
  }
}
