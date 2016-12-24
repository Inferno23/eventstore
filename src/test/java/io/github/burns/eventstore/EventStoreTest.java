package io.github.burns.eventstore;

import io.github.burns.eventstore.impl.EventStoreImpl;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.github.burns.eventstore.ExampleEventType.PUBLIC;


/**
 * Created by mike on 12/23/16.
 */
@RunWith(VertxUnitRunner.class)
public class EventStoreTest {
  private EventStore<ExampleEventType> eventStore;

  @Before
  public void before() {
    eventStore = new EventStoreImpl<>();
  }

  @Test(timeout = 2000L)
  public void publicEventSingleSubscriberTest(TestContext context) {
    final Async async = context.async();
    final Event<ExampleEventType> toPublish = new Event<>(1, PUBLIC);
    // Register for events
    eventStore.register()
        .subscribe(published -> {
          context.assertEquals(toPublish.id, published.id);
          context.assertEquals(toPublish.type, published.type);
          async.complete();
        }, context::fail);
    // Publish the event
    eventStore.publishEvent(toPublish);
  }
}
