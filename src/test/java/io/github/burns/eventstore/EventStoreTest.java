package io.github.burns.eventstore;

import io.github.burns.eventstore.impl.EventStoreImpl;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.functions.Actions;
import io.vertx.ext.unit.junit.VertxUnitRunner;


/**
 * Created by mike on 12/23/16.
 */
@RunWith(VertxUnitRunner.class)
public class EventStoreTest {
  private EventStore eventStore;

  @Before
  public void before() {
    eventStore = new EventStoreImpl();
  }

  @Test(timeout = 2000L)
  public void publishEventTest(TestContext context) {
    final Async async = context.async();
    eventStore.publishEvent(new Event())
        .subscribe(Actions.empty(), context::fail, async::complete);
  }
}
