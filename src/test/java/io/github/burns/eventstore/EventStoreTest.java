package io.github.burns.eventstore;

import io.github.burns.eventstore.impl.EventStoreImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Predicate;

import static io.github.burns.eventstore.ExampleEventScope.PRIVATE_1;
import static io.github.burns.eventstore.ExampleEventScope.PUBLIC;
import static io.github.burns.eventstore.ExampleEventType.ONE;
import static io.github.burns.eventstore.ExampleEventType.START;
import static io.github.burns.eventstore.ExampleEventType.TWO;


/**
 * Tests to validate the behavior of the implementation
 * of the EventStore interface.
 */
@RunWith(VertxUnitRunner.class)
public class EventStoreTest {
  public static final Predicate<ExampleEventScope> ALL_EVENTS_PREDICATE = s -> true;
  public static final long TIMEOUT = 2000L;
  private EventStore<ExampleEventType, ExampleEventScope, JsonObject> eventStore;

  @Before
  public void before() {
    eventStore = new EventStoreImpl<>();
  }

  @Test(timeout = TIMEOUT)
  public void publicEventSingleSubscriberTest(TestContext context) {
    final Async async = context.async();
    final int expectedId = eventStore.latestEventId() + 1;
    // Register for events
    eventStore.register(ALL_EVENTS_PREDICATE)
        .subscribe(event -> {
          context.assertEquals(expectedId, event.id);
          context.assertEquals(START, event.type);
          context.assertEquals(PUBLIC, event.scope);
          async.complete();
        }, context::fail);
    // Publish the event
    eventStore.publishEvent(START, PUBLIC);
  }

  @Test(timeout = TIMEOUT)
  public void twoEventsSingleSubscriberTest(TestContext context) {
    final Async asyncOne = context.async();
    final Async asyncTwo = context.async();
    final int expectedIdOne = eventStore.latestEventId() + 1;
    final int expectedIdTwo = expectedIdOne + 1;
    // Register for events
    eventStore.register(ALL_EVENTS_PREDICATE)
        .subscribe(event -> {
          if (event.id == expectedIdOne) {
             context.assertEquals(expectedIdOne, event.id);
             context.assertEquals(ONE, event.type);
             context.assertEquals(PUBLIC, event.scope);
             asyncOne.complete();
          } else if (event.id == expectedIdTwo) {
             context.assertEquals(expectedIdTwo, event.id);
             context.assertEquals(TWO, event.type);
             context.assertEquals(PUBLIC, event.scope);
             asyncTwo.complete();
          } else {
            context.fail("Unexpected id received: " + event.toString());
          }
        }, context::fail);
    // Publish the events
    eventStore.publishEvent(ONE, PUBLIC);
    eventStore.publishEvent(TWO, PUBLIC);
  }

  @Test(timeout = TIMEOUT)
  public void ignorePrivateEventTest(TestContext context) {
    final Async async = context.async();
    final int expectedIdOne = eventStore.latestEventId() + 1;
    final int expectedIdTwo = expectedIdOne + 1;
    final JsonObject content = new JsonObject().put("FOO", "BAR");
    // Register for only public events
    eventStore.register(PUBLIC::equals)
        .subscribe(event -> {
          if (event.id == expectedIdOne) {
            context.assertEquals(expectedIdOne, event.id);
            context.assertEquals(ONE, event.type);
            context.assertEquals(PRIVATE_1, event.scope);
            context.assertNull(event.content);
          } else if (event.id == expectedIdTwo) {
            context.assertEquals(expectedIdTwo, event.id);
            context.assertEquals(TWO, event.type);
            context.assertEquals(PUBLIC, event.scope);
            context.assertNotNull(event.content);
            context.assertEquals(content, event.content);
            async.complete();
          } else {
            context.fail("Unexpected id received: " + event.toString());
          }
        }, context::fail);
    // Publish the events
    eventStore.publishEvent(ONE, PRIVATE_1);
    eventStore.publishEvent(TWO, PUBLIC, content);
  }
}
