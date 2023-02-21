package ch.fhnw.apm.docfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

public class DocFinderPerfTester {

    private static final int REPETITIONS = 50;
    public static final String SEARCH_TEXT = "woman friend cat";

    public static void main(String[] args) throws InterruptedException {
        var booksDir = Path.of("doc-finder/perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }

        var finder = new DocFinder(booksDir);
        var latencies = new double[REPETITIONS];


        threads(finder, latencies);
        //        executor(finder, latencies);


        for (int i = 0; i < REPETITIONS/8; i++) {
            System.out.printf("%.1f\n", latencies[i]);
        }
        System.out.println();

        var stats = DoubleStream.of(latencies).summaryStatistics();
        System.out.printf("Average: %.1f ms\n", stats.getAverage());
        System.out.printf("Min: %.1f ms\n", stats.getMin());
        System.out.printf("Max: %.1f ms\n", stats.getMax());
    }

    static void mama(DocFinder finder, double[] latencies, int i, int numsPerThread) throws IOException {
        for (int j = i; j < i + numsPerThread; j++) {
            var startTime = System.nanoTime();

            finder.findDocs(SEARCH_TEXT);

            var latency = System.nanoTime() - startTime;

            // convert to ms
            latencies[j] = latency / 1_000_000.0;

            // print progress to err
            if ((i + 1) % 10 == 0) {
                System.err.println(i + 1 + "/" + REPETITIONS + " repetitions");
                System.err.println();
            }
        }
    }

    static void threads(DocFinder finder, double[] latencies) throws InterruptedException {
        var threads = new ArrayList<Thread>();
        var numsPerThread = REPETITIONS / 8;
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            var t = new Thread(() -> {
                try {
                    mama(finder, latencies, finalI, numsPerThread);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
            threads.add(t);
        }

        for (var t : threads) {
            t.join();
        }
    }

    static void executor(DocFinder finder, double[] latencies) throws InterruptedException {
        var executor = Executors.newFixedThreadPool(REPETITIONS);
        var numsPerThread = REPETITIONS / 8;
        for (int i = 0; i < REPETITIONS; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    mama(finder, latencies, finalI, numsPerThread);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

}