package ch.fhnw.apm.io;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newOutputStream;

@State(Scope.Benchmark)
public class FileIOBenchmarks {
    private static final Path BASE_DIR = Path.of("io-benchmarks/files");

    private static final Set<Integer> FILE_SIZES = Set.of(
            5_000_000,
            51_000_000,
            250_000_000);

    static {
        if (!exists(BASE_DIR)) {
            throw new AssertionError("'files' dir not found; is benchmark executed in correct dir?");
        }
        for (var size : FILE_SIZES) {
            var file = file(size);
            if (!exists(file)) {
                System.err.print("creating file '" + file + "'... ");
                createRandomFile(file, size);
                System.err.println("done.");
            }
        }
    }

    private static void createRandomFile(Path file, int size) {
        var random = new Random();
        try (var out = new BufferedOutputStream(newOutputStream(file))) {
            for (int i = 0; i < size; i++) {
                out.write(random.nextInt());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path file(int size) {
        if (!FILE_SIZES.contains(size)) {
            throw new AssertionError("invalid file size: " + size + "; add to FILE_SIZES before running the benchmark");
        }
        return BASE_DIR.resolve("file-" + size + ".bin");
    }

//    @Param({"5000000", "51000000", "250000000"})
    public int fileByte = 5000000;

    //    @Benchmark
    //    @BenchmarkMode(Mode.SampleTime)
    //    @Warmup(iterations = 1)
    //    @Measurement(iterations = 2)
    //    public int read0() throws IOException {
    //        System.out.println("InputStream and file size: " + fileByte);
    //        try (var in = Files.newInputStream(file(fileByte))) {
    //            int byteZeroCount = 0;
    //            int b;
    //            while ((b = in.read()) >= 0) {
    //                if (b == 0) {
    //                    byteZeroCount++;
    //                }
    //            }
    //            return byteZeroCount;
    //        }
    //    }
    //
    //    @Benchmark
    //    @BenchmarkMode(Mode.SampleTime)
    //    @Warmup(iterations = 1)
    //    @Measurement(iterations = 2)
    //    public int read1() throws IOException {
    //        System.out.println("BufferedInputStream and file size: " + fileByte);
    //        try (var in = new BufferedInputStream(Files.newInputStream(file(fileByte)))) {
    //            int byteZeroCount = 0;
    //            int b;
    //            while ((b = in.read()) >= 0) {
    //                if (b == 0) {
    //                    byteZeroCount++;
    //                }
    //            }
    //            return byteZeroCount;
    //        }
    //    }


    @Param({"8192", "16384", "32768"})
    private int bufferSize;

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @Warmup(iterations = 1)
    @Measurement(iterations = 2)
    public int read2() throws IOException {
        try (var in = Files.newInputStream(file(fileByte))) {
            int byteZeroCount = 0;
            var buffer = in.readNBytes(bufferSize);
            while (buffer.length != 0) {
                for (byte b : buffer) {
                    if (b == 0) {
                        byteZeroCount++;
                    }
                }
                buffer = in.readNBytes(8192);
            }
            return byteZeroCount;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FileIOBenchmarks.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}