package io.github.burns.eventstore;

import io.github.burns.eventstore.impl.EventProcessor;
import io.github.burns.eventstore.impl.EventStoreFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.functions.Action1;

import java.util.function.Predicate;

import static io.github.burns.eventstore.ExampleEventScope.PRIVATE_1;
import static io.github.burns.eventstore.ExampleEventScope.PRIVATE_2;
import static io.github.burns.eventstore.ExampleEventScope.PUBLIC;
import static io.github.burns.eventstore.ExampleEventType.ONE;
import static io.github.burns.eventstore.ExampleEventType.THREE;
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
    eventStore = EventStoreFactory.getInstance();
  }

  @After
  public void after() {
    EventStoreFactory.closeInstance();
  }

  @Test(timeout = TIMEOUT)
  public void publicEventSingleSubscriberTest(TestContext context) {
    final Async async = context.async();
    final int expectedId = eventStore.latestEventId() + 1;
    // Register for events
    eventStore.register(ALL_EVENTS_PREDICATE)
        .subscribe(event -> {
          context.assertEquals(expectedId, event.id);
          context.assertEquals(ONE, event.type);
          context.assertEquals(PUBLIC, event.scope);
          async.complete();
        }, context::fail);
    // Publish the event
    eventStore.publishEvent(ONE, PUBLIC);
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
    eventStore.publishEvent(ONE, PRIVATE_1, content);
    eventStore.publishEvent(TWO, PUBLIC, content);
  }

  @Test(timeout = TIMEOUT)
  public void replayEventsIgnoringContentTest(TestContext context) {
    final Async asyncOne = context.async(2);
    final Async asyncTwo = context.async(2);
    final int startingId = eventStore.latestEventId();
    final int expectedIdOne = startingId + 1;
    final int expectedIdTwo = expectedIdOne + 1;
    // Register for events
    eventStore.register(ALL_EVENTS_PREDICATE)
        .subscribe(event -> {
          if (event.id == expectedIdOne) {
            context.assertEquals(expectedIdOne, event.id);
            context.assertEquals(ONE, event.type);
            context.assertEquals(PUBLIC, event.scope);
            asyncOne.countDown();
          } else if (event.id == expectedIdTwo) {
            context.assertEquals(expectedIdTwo, event.id);
            context.assertEquals(TWO, event.type);
            context.assertEquals(PUBLIC, event.scope);
            asyncTwo.countDown();
          } else {
            context.fail("Unexpected id received: " + event.toString());
          }
        }, context::fail);
    // Publish some events
    eventStore.publishEvent(ONE, PUBLIC);
    eventStore.publishEvent(TWO, PUBLIC);
    // Replay the events
    eventStore.getEvents(startingId);
  }

  @Test(timeout = TIMEOUT)
  public void replayEventsWithContentTest(TestContext context) {
    final Async asyncOne = context.async(2);
    final Async asyncTwo = context.async(2);
    final int startingId = eventStore.latestEventId();
    final int expectedIdOne = startingId + 1;
    final int expectedIdTwo = expectedIdOne + 1;
    final JsonObject content = new JsonObject().put("FOO", "BAR");
    // Register for events
    eventStore.register(PUBLIC::equals)
        .subscribe(event -> {
          if (event.id == expectedIdOne) {
            context.assertEquals(expectedIdOne, event.id);
            context.assertEquals(ONE, event.type);
            context.assertEquals(PRIVATE_1, event.scope);
            context.assertNull(event.content);
            asyncOne.countDown();
          } else if (event.id == expectedIdTwo) {
            context.assertEquals(expectedIdTwo, event.id);
            context.assertEquals(TWO, event.type);
            context.assertEquals(PUBLIC, event.scope);
            context.assertNotNull(event.content);
            context.assertEquals(content, event.content);
            asyncTwo.countDown();
          } else {
            context.fail("Unexpected id received: " + event.toString());
          }
        }, context::fail);
    // Publish some events
    eventStore.publishEvent(ONE, PRIVATE_1, content);
    eventStore.publishEvent(TWO, PUBLIC, content);
    // Replay the events
    eventStore.getEvents(startingId);
  }

  @Test(timeout = TIMEOUT)
  public void initializeProcessorThenPublishEventTest(TestContext context) {
    final Async async = context.async();
    new EventProcessor<>(ALL_EVENTS_PREDICATE, event -> async.complete());
    eventStore.publishEvent(ONE, PUBLIC);
  }

  @Test(timeout = TIMEOUT)
  public void initializeProcessorAfterPublishingEventsTest(TestContext context) {
    final Async async = context.async();
    eventStore.publishEvent(ONE, PUBLIC);
    new EventProcessor<>(ALL_EVENTS_PREDICATE, event -> async.complete());
  }

  @Test(timeout = TIMEOUT)
  public void eventProcessorWithPredicateTest(TestContext context) {
    final Async async = context.async(2);
    new EventProcessor<>(PUBLIC::equals, countDownIfContentIsNonNull(async));
    eventStore.publishEvent(ONE, PUBLIC, new JsonObject());
    eventStore.publishEvent(TWO, PUBLIC);
    eventStore.publishEvent(THREE, PUBLIC, new JsonObject());
  }

  @Test(timeout = TIMEOUT)
  public void multipleEventProcessorsTest(TestContext context) {
    final Async asyncOne = context.async();
    final Async asyncTwo = context.async();
    new EventProcessor<>(PRIVATE_1::equals, countDownIfContentIsNonNull(asyncOne));
    new EventProcessor<>(PRIVATE_2::equals, countDownIfContentIsNonNull(asyncTwo));
    eventStore.publishEvent(ONE, PRIVATE_1, new JsonObject());
    eventStore.publishEvent(TWO, PRIVATE_2, new JsonObject());
  }

  /**
   * Helper to produce an action which consumes an event, and
   * counts down an Async instance if the event has non-null content.
   *
   * @param async The async to potentially count down.
   * @return The Action to pass in to an EventProcessor.
   */
  private Action1<Event<Object, Object, Object>> countDownIfContentIsNonNull(Async async) {
    return event -> {
      if (event.content != null) {
        async.countDown();
      }
    };
  }
}
