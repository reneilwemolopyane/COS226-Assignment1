import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.LinkedHashMap;
import java.util.Map;

public class Experiment
{
    private static final int[] THREAD_COUNTS = {2, 4, 8, 16};
    private static final int ITERATIONS = 20000;
    private static final int REPEATS = 3;
    private static final String RAW_FILE = "results.txt";
    private static final String SUMMARY_FILE = "summary.txt";
    private static final String ROW_FORMAT = "%-6s %14s %18s %18s %14s %20s%n";

    public static void main(String[] args) throws InterruptedException, IOException
    {
        Map<String, Supplier<Lock>> locks = new LinkedHashMap<>();
        locks.put("TTAS", TTAS::new);
        locks.put("CLH", CHL::new);
        locks.put("MCS", MCS::new);

        PrintWriter raw = new PrintWriter(new FileWriter(RAW_FILE));
        PrintWriter summary = new PrintWriter(new FileWriter(SUMMARY_FILE));

        raw.println("lock,threads,iterations,run,executionTimeMs,totalBids,expectedBids,finalHighestBid,avgWaitNs,minBidsWon,maxBidsWon");

        for (int threadCount : THREAD_COUNTS)
        {
            long expected = (long) threadCount * ITERATIONS;
            summary.println(threadCount + " bidder threads (" + expected + " expected bids)");
            printRow(summary, "Lock", "Mean exec(ms)", "Total bids placed", "Final highest bid", "Mean wait(ns)", "Bids won/bidder");
            summary.flush();

            for (Map.Entry<String, Supplier<Lock>> entry : locks.entrySet())
            {
                String lockName = entry.getKey();
                Supplier<Lock> factory = entry.getValue();

                double execSum = 0;
                double waitSum = 0;
                long totalBidsSum = 0;
                double finalBidSum = 0;
                boolean bidsUniform = true;
                long bidsPerBidder = -1;

                for (int run = 1; run <= REPEATS; run++)
                {
                    Auction auction = new Auction("Test Item");
                    Lock lock = factory.get();
                    Runner runner = new Runner(threadCount, ITERATIONS, auction, lock, false);

                    runner.run();

                    int minWon = Integer.MAX_VALUE;
                    int maxWon = Integer.MIN_VALUE;
                    for (int i = 0; i < threadCount; i++)
                    {
                        int won = runner.getBidsWon(i);
                        minWon = Math.min(minWon, won);
                        maxWon = Math.max(maxWon, won);
                    }
                    if (minWon != maxWon)
                    {
                        bidsUniform = false;
                    }
                    bidsPerBidder = minWon;

                    double execMs = runner.getExecutionTimeNanos() / 1_000_000.0;

                    execSum += execMs;
                    waitSum += runner.getAverageWaitNanos();
                    totalBidsSum += runner.getTotalBids();
                    finalBidSum += runner.getFinalHighestBid();

                    raw.printf(Locale.US,
                        "%s,%d,%d,%d,%.3f,%d,%d,%.1f,%.1f,%d,%d%n",
                        lockName, threadCount, ITERATIONS, run, execMs,
                        runner.getTotalBids(), expected, runner.getFinalHighestBid(),
                        runner.getAverageWaitNanos(), minWon, maxWon
                    );
                    raw.flush();
                }

                double meanExec = execSum / REPEATS;
                double meanWait = waitSum / REPEATS;
                long meanTotalBids = totalBidsSum / REPEATS;
                double meanFinalBid = finalBidSum / REPEATS;
                String wonStr = bidsUniform ? (bidsPerBidder + " (uniform)") : "varied";

                printRow(summary, lockName,
                    String.format(Locale.US, "%.1f", meanExec),
                    String.valueOf(meanTotalBids),
                    String.format(Locale.US, "%.0f", meanFinalBid),
                    String.format(Locale.US, "%.1f", meanWait),
                    wonStr);
                summary.flush();
            }

            summary.println();
            summary.flush();
        }

        raw.close();
        summary.close();
        System.out.println("Raw per-run data written to " + RAW_FILE);
        System.out.println("Averaged summary written to " + SUMMARY_FILE);
    }

    private static void printRow(PrintWriter out, String... columns)
    {
        out.printf(Locale.US, ROW_FORMAT, (Object[]) columns);
    }
}
