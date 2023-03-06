package ch.fhnw.apm.docfinder;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@State(Scope.Benchmark)
public class DocFinderBenchmarks {

    @Param({"woman friend cat", "king alice", "penguin"})
    public String searchText;
    public static final String SEARCH_TEXT = "woman friend cat";
    private DocFinder finder;

    @Setup
    public void setUp() {
        var booksDir = Path.of("doc-finder/perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }
        finder = new DocFinder(booksDir, 16);
    }

    @Warmup(iterations = 1)
    @Measurement(iterations = 3, time = 5)
    @BenchmarkMode(Mode.SampleTime)
    @Benchmark
    public void findDocs() throws IOException {
        finder.findDocs(searchText);
        //finder.findDocs(SEARCH_TEXT);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DocFinderBenchmarks.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }

}
