import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

public class FixedThreadPoolTest {

    @Test
    void testChecksCorrectnessWhenManyThreadsAreWorking() {
        //given
        Counter counter = new Counter();

        int numThreads = 3;
        FixedThreadPool pool = new FixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Runnable task = counter::inc;
            pool.execute(task);
        }
        pool.start();
        for (int i = 0; i < numThreads; i++) {
            Runnable task = counter::inc;
            pool.execute(task);
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pool.stop();
        //when
        int expectedCounterValue = 6;
        //then
        assertEquals(expectedCounterValue, counter.getN());
    }

    @Test
    void testChecksTaskAreNotRunUntilStartMethodInvoked() {
        Counter counter = new Counter();

        int numThreads = 3;
        FixedThreadPool pool = new FixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Runnable task = counter::inc;
            pool.execute(task);
        }
        int expectedCounterValue = 0;
        assertEquals(expectedCounterValue, counter.getN());
    }
}
