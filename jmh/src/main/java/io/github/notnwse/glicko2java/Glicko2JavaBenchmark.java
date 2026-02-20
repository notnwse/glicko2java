package io.github.notnwse.glicko2java;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class Glicko2JavaBenchmark {

  private static final int MATCHES = 10;

  @Param({"100", "1000", "10000", "100000", "1000000"})
  private int size;

  private Glicko2 glicko2;
  private Data[] data;

  private record Data(Rating player, List<Match> matches) {
  }

  @Setup(Level.Trial)
  public void setup() {
    this.glicko2 = Glicko2.create();
    this.data = new Data[this.size];

    final Random random = new Random(42);
    for (int idx0 = 0; idx0 < this.size; idx0++) {
      final double rating = random.nextDouble() * 1200;
      final double deviation = random.nextDouble() * 320;
      final double volatility = random.nextDouble() * 0.04;
      final Rating player = Rating.of(rating, deviation, volatility);

      final List<Match> matches = new ArrayList<>(MATCHES);
      for (int idx1 = 0; idx1 < MATCHES; idx1++) {
        final double opponentRating = random.nextDouble() * 1200;
        final double opponentDeviation = random.nextDouble() * 320;
        final double score = random.nextInt(3) * 0.5;
        matches.add(Match.of(
            Rating.of(opponentRating, opponentDeviation, Rating.DEFAULT_VOLATILITY), score));
      }
      this.data[idx0] = new Data(player, matches);
    }
  }

  @Benchmark
  public void processBatch(Blackhole hole) {
    for (int idx = 0; idx < this.size; idx++) {
      final Data data = this.data[idx];
      final Rating result = this.glicko2.recalculate(data.player, data.matches);
      hole.consume(result);
    }
  }

  public static void main(String[] args) throws RunnerException {
    final Options options = new OptionsBuilder()
        .include(Glicko2JavaBenchmark.class.getSimpleName())
        .addProfiler(GCProfiler.class)
        .build();
    new Runner(options).run();
  }
}
