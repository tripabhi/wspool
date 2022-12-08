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

        int warmup = 10, record = 10;
        double average = 0.0f, averageControl = 0.0f;
        for(int i = 0; i < warmup + record; i++) {
            long[] t = runner.run(task, threadCount);
            if(i >= warmup) {
                average += t[0];
                averageControl += t[1];
            }
        }

        average /= record;
        averageControl /= record;

        System.out.println("Benchmarking done");
        System.out.println("Task : " + task + ", threads : " + threadCount);
        System.out.printf("(%d, %.2f)", threadCount, average);
        System.out.printf("(%d, %.2f)", threadCount, averageControl);
    }
}
