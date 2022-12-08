package bench;

import bench.runner.BenchmarkRunner;

import java.util.stream.IntStream;

public class Benchmark {

    public static void main(String[] args) {
        if(args.length < 2) {
            System.out.println("""
                    Invalid usage
                    Usage: java Benchmark <task> <thread-count>
                    """);
            return;
        }

        String task = args[0];
        int threadCount = Integer.parseInt(args[1]);
        BenchmarkRunner runner = new BenchmarkRunner();

        int iterations = 10;
        IntStream.range(0, iterations).forEach(i -> runner.run(task, threadCount));
        System.out.println("Benchmarking done");
    }
}
