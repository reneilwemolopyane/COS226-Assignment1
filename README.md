# COS 226 Assignment 1

Group members: [Add student numbers here]

## Overview

Implementation of TTAS, CLH, and MCS locks (Task 1), a concurrent auction using those
locks (Task 2), and an experiment comparing the three locks under increasing contention
(Task 3).

## Files

- `Lock.java` — lock interface
- `TTAS.java` — Test-and-Test-and-Set lock
- `CHL.java` — CLH queue lock
- `MCS.java` — MCS queue lock
- `Auction.java` — the auction being bid on
- `AuctionUtils.java` — random item name generator
- `Runner.java` — creates/runs bidder threads, collects metrics
- `Main.java` — single demo run
- `Experiment.java` — full Task 3 experiment (all locks x all thread counts x 3 repeats)
- `Makefile` — build/run/clean targets

## Build and run

```
javac Lock.java Auction.java AuctionUtils.java TTAS.java CHL.java MCS.java Runner.java Main.java Experiment.java
java Main
java Experiment
```

Cleaning without `make` (PowerShell): `Remove-Item *.class`

## Task 3 results

Running `Experiment` (20,000 iterations/thread, 3 repeats per configuration) writes:

- `results.txt` — raw per-run data
- `summary.txt` — averaged results per (lock, thread count)
