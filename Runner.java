import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Runner
{
    public final int numberOfThreads;
    public final int iterations;
    public final Auction auction;
    public final Lock lock;
    private final boolean verbose; // whether run() prints a report automatically

    private final AtomicLong totalBids = new AtomicLong(0); // successful bids placed across all threads
    private final AtomicLong totalWaitingTimeNanos = new AtomicLong(0); // sum of time spent waiting for lock.lock() to return
    private final AtomicIntegerArray bidsWon; // bids won per bidder, indexed by bidderId
    private long executionTimeNanos; // total wall-clock time for the last run() call

    // Default constructor: verbose, prints results automatically (used by Main.java)
    public Runner(int numberOfThreads, int iterations, Auction auction, Lock lock)
    {
        this(numberOfThreads, iterations, auction, lock, true);
    }

    // Full constructor: verbose=false is used by Experiment.java so it can collect
    // metrics via the getters below instead of having every run print to the console
    public Runner(int numberOfThreads, int iterations, Auction auction, Lock lock, boolean verbose)
    {
        this.numberOfThreads = numberOfThreads;
        this.iterations = iterations;
        this.auction = auction;
        this.lock = lock;
        this.verbose = verbose;
        this.bidsWon = new AtomicIntegerArray(numberOfThreads);
    }

    // Creates one thread per bidder, runs them all concurrently, times the whole thing,
    // and (if verbose) prints the results once every thread has finished
    public void run() throws InterruptedException
    {
        Thread[] threads = new Thread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; i++)
        {
            final int bidderId = i; // must be captured as final for the lambda below

            threads[i] = new Thread(() -> {
                bidder(bidderId);
            });
        }

        long startTime = System.nanoTime();

        for (Thread thread : threads)
        {
            thread.start(); // kick off every bidder thread
        }

        for (Thread thread : threads)
        {
            thread.join(); // wait here until every bidder thread has finished
        }

        long endTime = System.nanoTime();
        executionTimeNanos = endTime - startTime;

        if (verbose)
        {
            reportResults(executionTimeNanos);
        }
    }

    // What one bidder thread does: repeatedly try to become the new highest bidder.
    // The read of the current highest bid, the decision on the new bid, and the write
    // are all done while holding the lock, so the whole operation is atomic and every
    // attempt is guaranteed to succeed under correct mutual exclusion.
    public void bidder(int bidderId)
    {
        for (int i = 0; i < iterations; i++)
        {
            long waitStart = System.nanoTime();
            lock.lock(); // blocks here until this thread gets the lock
            long waitEnd = System.nanoTime();
            totalWaitingTimeNanos.addAndGet(waitEnd - waitStart); // record how long we waited

            try
            {
                double currentHighest = auction.getHighestBid();
                double newBid = currentHighest + 1.0;
                auction.placeBid(bidderId, newBid);
                totalBids.incrementAndGet();
                bidsWon.incrementAndGet(bidderId);
            }
            finally
            {
                lock.unlock(); // always release, even if something above throws
            }
        }
    }

    // Prints a full summary of one run to the console: timing, final auction state,
    // sanity checks (expected vs actual bids), the extra lock-wait metric, and a
    // per-bidder breakdown of how many bids each one won
    public void reportResults(long executionTime)
    {
        long expectedBids = (long) numberOfThreads * iterations;
        long actualBids = totalBids.get();
        double avgWaitNanos = getAverageWaitNanos();

        System.out.println("Execution time (ms): " + executionTime / 1_000_000.0);
        System.out.println("Item: " + auction.getItemName());
        System.out.println("Final highest bid: " + auction.getHighestBid());
        System.out.println("Final highest bidder: " + auction.getHighestBidder());
        System.out.println("Expected total bids: " + expectedBids);
        System.out.println("Total bids placed: " + actualBids);
        System.out.println("Average lock wait time (ns): " + avgWaitNanos);

        for (int i = 0; i < numberOfThreads; i++)
        {
            System.out.println("Bidder " + i + " bids won: " + bidsWon.get(i));
        }
    }

    // Getters below let Experiment.java (or anything else) pull the measurements out
    // programmatically instead of parsing the printed report

    public long getExecutionTimeNanos()
    {
        return executionTimeNanos;
    }

    public long getTotalBids()
    {
        return totalBids.get();
    }

    // Average time (ns) a thread spent blocked in lock.lock() before it returned,
    // across every successful bid -- this is the "additional measurement"
    public double getAverageWaitNanos()
    {
        long bids = totalBids.get();
        return bids == 0 ? 0.0 : totalWaitingTimeNanos.get() / (double) bids;
    }

    public int getBidsWon(int bidderId)
    {
        return bidsWon.get(bidderId);
    }

    public double getFinalHighestBid()
    {
        return auction.getHighestBid();
    }
}
