package ch.fhnw.apm.docfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

public class DocFinderPerfTester {

    private static final int REPETITIONS = 500;
    public static final String SEARCH_TEXT = "woman friend cat";

    public static void main(String[] args) throws IOException {
        var booksDir = Path.of("doc-finder/perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }
        List<Result> finds = new ArrayList<>();
        var finder = new DocFinder(booksDir, 16);

        var latencies = new double[REPETITIONS];
        for (int i = 0; i < REPETITIONS; i++) {
            var startTime = System.nanoTime();

            finds = finder.findDocs(SEARCH_TEXT);

            var latency = System.nanoTime() - startTime;
            latencies[i] = latency / 1_000_000.0; // convert to ms

            // print progress to err
            if ((i + 1) % 10 == 0) {
                System.err.println(i + 1 + "/" + REPETITIONS + " repetitions");
            }
        }
        System.err.println();

        for (int i = 0; i < REPETITIONS; i++) {
            System.out.printf("%.1f\n", latencies[i]);
        }

        //        for (Result result : finds) {
        //            String output = result.getDoc().toString().substring(79) + "\n";
        //
        //            for (String key : result.getSearchHits().keySet()) {
        //                output += result.totalHits() + "\n";
        //            }
        //
        //            System.out.println(output);
        //        }

        System.out.println();

        var stats = DoubleStream.of(latencies).summaryStatistics();
        System.out.printf("Average: %.1f ms\n", stats.getAverage());
        System.out.printf("Min: %.1f ms\n", stats.getMin());
        System.out.printf("Max: %.1f ms\n", stats.getMax());
    }
}
