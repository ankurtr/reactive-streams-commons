package rsc.publisher;

import org.junit.Assert;
import org.junit.Test;
import rsc.test.TestSubscriber;

public class PublisherNeverTest {

    @Test
    public void singleInstance() {
        Assert.assertSame(PublisherNever.instance(), PublisherNever.instance());
    }

    @Test
    public void normal() {
        TestSubscriber<Integer> ts = new TestSubscriber<>();

        PublisherNever.<Integer>instance().subscribe(ts);

        ts
          .assertSubscribed()
          .assertNoValues()
          .assertNoError()
          .assertNotComplete();
    }
}
