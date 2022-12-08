# Work Stealing Threadpool Java Implementation

## Prerequisites
### Java version 18.0.2.1

Running `java -version` should look like:
```
➜  wspool git:(main) ✗ java -version
openjdk version "18.0.2.1" 2022-08-18
OpenJDK Runtime Environment Homebrew (build 18.0.2.1+0)
OpenJDK 64-Bit Server VM Homebrew (build 18.0.2.1+0, mixed mode, sharing)
```

## Build
Build the project using `./gradlew build`

## Run Benchmarks
Benchmarks can be run using
```
java -cp build/libs/wspool-1.0-SNAPSHOT.jar bench.Benchmark $TASK $THREAD_COUNT;
```

`$TASK` can be `{MergeSort|Fibonacci|ArrayTransform|PrimeCounter}`