package io.github.burns.eventstore;

/**
 * Created by mike on 12/24/16.
 */
public class ExampleEvent extends Event<ExampleEventType, ExampleEventScope> {
  /**
   * Construct an Event.
   *
   * @param id    The id number of the event.
   * @param type  The type of event.
   * @param scope The scope of the event.
   */
  public ExampleEvent(int id, ExampleEventType type, ExampleEventScope scope) {
    super(id, type, scope);
  }
}
