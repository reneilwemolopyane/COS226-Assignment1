/*public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        int numberOfThreads = 4;
        int iterations = 200;

        Auction auction = new Auction(AuctionUtils.generateItemName());
        Lock lock = new TTAS();
        Runner runner = new Runner(numberOfThreads, iterations, auction, lock);
        runner.run();
    }
}*/


public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        int iterations = 200;

        System.out.println("=== 2 threads ===");

        System.out.println("--- TTAS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new TTAS();
            Runner runner = new Runner(2, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- CLH ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new CHL();
            Runner runner = new Runner(2, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- MCS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new MCS();
            Runner runner = new Runner(2, iterations, auction, lock);
            runner.run();
        }

        System.out.println("=== 4 threads ===");

        System.out.println("--- TTAS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new TTAS();
            Runner runner = new Runner(4, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- CLH ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new CHL();
            Runner runner = new Runner(4, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- MCS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new MCS();
            Runner runner = new Runner(4, iterations, auction, lock);
            runner.run();
        }

        System.out.println("=== 8 threads ===");

        System.out.println("--- TTAS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new TTAS();
            Runner runner = new Runner(8, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- CLH ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new CHL();
            Runner runner = new Runner(8, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- MCS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new MCS();
            Runner runner = new Runner(8, iterations, auction, lock);
            runner.run();
        }

        System.out.println("=== 16 threads ===");

        System.out.println("--- TTAS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new TTAS();
            Runner runner = new Runner(16, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- CLH ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new CHL();
            Runner runner = new Runner(16, iterations, auction, lock);
            runner.run();
        }

        System.out.println("--- MCS ---");
        {
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Lock lock = new MCS();
            Runner runner = new Runner(16, iterations, auction, lock);
            runner.run();
        }
    }
}
