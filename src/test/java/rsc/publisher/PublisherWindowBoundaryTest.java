package rsc.publisher;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import rsc.processor.DirectProcessor;
import rsc.test.TestSubscriber;
import rsc.util.ConstructorTestBuilder;

public class PublisherWindowBoundaryTest {

    @Test
    public void constructors() {
        ConstructorTestBuilder ctb = new ConstructorTestBuilder(PublisherWindowBoundary.class);
        
        ctb.addRef("source", PublisherNever.instance());
        ctb.addRef("other", PublisherNever.instance());
        ctb.addRef("processorQueueSupplier", (Supplier<Queue<Object>>)() -> new ConcurrentLinkedQueue<>());
        ctb.addRef("drainQueueSupplier", (Supplier<Queue<Object>>)() -> new ConcurrentLinkedQueue<>());
        
        ctb.test();
    }

    static <T> TestSubscriber<T> toList(Publisher<T> windows) {
        TestSubscriber<T> ts = new TestSubscriber<>();
        windows.subscribe(ts);
        return ts;
    }

    @SafeVarargs
    static <T> void expect(TestSubscriber<Px<T>> ts, int index, T... values) {
        toList(ts.values().get(index))
        .assertValues(values)
        .assertComplete()
        .assertNoError();
    }

    @Test
    public void normal() {
        TestSubscriber<Px<Integer>> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        
        sp1.window(sp2).subscribe(ts);
        
        ts.assertValueCount(1);
        
        sp1.onNext(1);
        sp1.onNext(2);
        sp1.onNext(3);
        
        sp2.onNext(1);
        
        sp1.onNext(4);
        sp1.onNext(5);
        
        sp1.onComplete();
        
        ts.assertValueCount(2);

        expect(ts, 0, 1, 2, 3);
        expect(ts, 1, 4, 5);
        
        ts.assertNoError()
        .assertComplete();
        
        Assert.assertFalse("sp1 has subscribers", sp1.hasDownstreams());
        Assert.assertFalse("sp2 has subscribers", sp1.hasDownstreams());
    }

    @Test
    public void normalOtherCompletes() {
        TestSubscriber<Px<Integer>> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        
        sp1.window(sp2).subscribe(ts);
        
        ts.assertValueCount(1);
        
        sp1.onNext(1);
        sp1.onNext(2);
        sp1.onNext(3);
        
        sp2.onNext(1);
        
        sp1.onNext(4);
        sp1.onNext(5);
        
        sp2.onComplete();
        
        ts.assertValueCount(2);

        expect(ts, 0, 1, 2, 3);
        expect(ts, 1, 4, 5);
        
        ts.assertNoError()
        .assertComplete();
        
        Assert.assertFalse("sp1 has subscribers", sp1.hasDownstreams());
        Assert.assertFalse("sp2 has subscribers", sp1.hasDownstreams());
    }

    @Test
    public void mainError() {
        TestSubscriber<Px<Integer>> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        
        sp1.window(sp2).subscribe(ts);
        
        ts.assertValueCount(1);
        
        sp1.onNext(1);
        sp1.onNext(2);
        sp1.onNext(3);
        
        sp2.onNext(1);
        
        sp1.onNext(4);
        sp1.onNext(5);
        
        sp1.onError(new RuntimeException("forced failure"));
        
        ts.assertValueCount(2);

        expect(ts, 0, 1, 2, 3);
        
        toList(ts.values().get(1))
        .assertValues(4, 5)
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        ts
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        Assert.assertFalse("sp1 has subscribers", sp1.hasDownstreams());
        Assert.assertFalse("sp2 has subscribers", sp1.hasDownstreams());
    }

    @Test
    public void otherError() {
        TestSubscriber<Px<Integer>> ts = new TestSubscriber<>();
        
        DirectProcessor<Integer> sp1 = new DirectProcessor<>();
        DirectProcessor<Integer> sp2 = new DirectProcessor<>();
        
        sp1.window(sp2).subscribe(ts);
        
        ts.assertValueCount(1);
        
        sp1.onNext(1);
        sp1.onNext(2);
        sp1.onNext(3);
        
        sp2.onNext(1);
        
        sp1.onNext(4);
        sp1.onNext(5);
        
        sp2.onError(new RuntimeException("forced failure"));
        
        ts.assertValueCount(2);

        expect(ts, 0, 1, 2, 3);
        
        toList(ts.values().get(1))
        .assertValues(4, 5)
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        ts
        .assertError(RuntimeException.class)
        .assertErrorMessage("forced failure")
        .assertNotComplete();
        
        Assert.assertFalse("sp1 has subscribers", sp1.hasDownstreams());
        Assert.assertFalse("sp2 has subscribers", sp1.hasDownstreams());
    }

}
