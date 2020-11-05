import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class ScalableThreadPoolTest {
    @Test
    void testChecksTaskAreNotRunUntilStartMethodInvoked() {
        Counter counter = new Counter();

        int minThreads = 3;
        int maxThreads = 5;
        ScalableThreadPool pool = new ScalableThreadPool(minThreads, maxThreads);

        for (int i = 0; i < 2 * maxThreads; i++) {
            Runnable task = counter::inc;
            pool.execute(task);
        }
        int expectedCounterValue = 0;
        assertEquals(expectedCounterValue, counter.getN());
    }
}
