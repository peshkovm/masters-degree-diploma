package com.github.peshkovm.main.operationbased.gcounter;

import com.github.peshkovm.common.component.LifecycleComponent;
import com.github.peshkovm.crdt.CrdtService;
import com.github.peshkovm.crdt.operationbased.GCounterCmRDT;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.node.InternalClusterFactory;
import com.github.peshkovm.node.InternalNode;
import com.github.peshkovm.raft.discovery.ClusterDiscovery;
import com.github.peshkovm.transport.netty.NettyTransportService;
import io.vavr.collection.Vector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class PerformanceTest {
  private static final int NUM_ITERATIONS = 20;
  private static final int NUM_OF_FORKS = 2;
  private static final int TIMES_TO_INCREMENT = 50_000;
  private static final long NUM_OF_SECONDS_TO_WAIT = TimeUnit.SECONDS.toMicros(5);
  private static final String RES_FILE_PATH =
      "src/main/resources/main/operationbased/gcounter/PerformanceTest.csv";

  @State(Scope.Benchmark)
  public abstract static class CrdtState {
    protected Vector<InternalNode> nodes = Vector.empty();
    protected Vector<CrdtService> crdtServices;

    @Setup(Level.Trial)
    public void init0() {
      init();
    }

    @Setup(Level.Iteration)
    public void prepareSet0() {
      prepareSet();
    }

    @TearDown(Level.Trial)
    public void tearDownNodes() {
      nodes.forEach(LifecycleComponent::stop);
      nodes.forEach(LifecycleComponent::close);
      nodes = Vector.empty();
      InternalClusterFactory.reset();
    }

    public abstract void init();

    public abstract void prepareSet();

    protected final void createAndStartInternalNode() {
      final InternalNode node = InternalClusterFactory.createInternalNode();
      nodes = nodes.append(node);

      node.start();
    }

    protected final void changeLoggerLevel(org.apache.logging.log4j.Level level) {
      Configurator.setAllLevels(LogManager.getRootLogger().getName(), level);
    }

    protected void connectAllNodes() {
      for (int i = 0; i < nodes.size(); i++) {
        final InternalNode sourceNode = nodes.get(i);

        final NettyTransportService transportService =
            sourceNode.getBeanFactory().getBean(NettyTransportService.class);

        for (int j = 0; j < nodes.size() - 1; j++) {
          transportService.connectToNode(
              nodes
                  .get((i + 1 + j) % nodes.size())
                  .getBeanFactory()
                  .getBean(ClusterDiscovery.class)
                  .getSelf());
        }
      }
    }

    protected void createResource(String crdtId, ResourceType crdtType) {
      crdtServices.head().addResource(crdtId, crdtType).get();
    }

    protected void deleteResource(String crdtId, ResourceType crdtType) {
      crdtServices.head().deleteResource(crdtId, crdtType).get();
    }
  }

  public static class GCounterCmrdtState extends CrdtState {
    protected final String crdtId = "countOfLikes";
    protected Vector<GCounterCmRDT> gCounters;

    @Override
    public void init() {
      changeLoggerLevel(org.apache.logging.log4j.Level.WARN);
      createAndStartInternalNode();
      createAndStartInternalNode();
      createAndStartInternalNode();

      connectAllNodes();

      crdtServices = nodes.map(node -> node.getBeanFactory().getBean(CrdtService.class));
    }

    @Override
    public void prepareSet() {
      createResource(crdtId, ResourceType.GCounterCmRDT);

      gCounters =
          crdtServices
              .map(CrdtService::crdtRegistry)
              .map(crdtRegistry -> crdtRegistry.crdt(crdtId, GCounterCmRDT.class));
    }

    @TearDown(Level.Iteration)
    public void check(BenchmarkParams params) throws InterruptedException {
      gCounters.forEach(
          counter -> {
            if (counter.query() != TIMES_TO_INCREMENT) {
              throw new AssertionError(
                  "\nExpected :" + TIMES_TO_INCREMENT + "\nActual   :" + counter.query());
            }
          });

      deleteResource(crdtId, ResourceType.GCounterCmRDT);
    }
  }

  @Fork(NUM_OF_FORKS)
  @Warmup(iterations = NUM_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
  @Measurement(iterations = NUM_ITERATIONS, timeUnit = TimeUnit.MILLISECONDS)
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public static class GCounterTest {

    @Threads(1)
    @Warmup(batchSize = TIMES_TO_INCREMENT)
    @Measurement(batchSize = TIMES_TO_INCREMENT)
    @Benchmark
    public void increment(final GCounterCmrdtState state, final Blackhole bh)
        throws InterruptedException {
      final GCounterCmRDT sourceGCounter = state.gCounters.get(0);
      sourceGCounter.increment();

      if (sourceGCounter.query() == TIMES_TO_INCREMENT) {
        for (int i = 0; i < NUM_OF_SECONDS_TO_WAIT / 100; i++) {
          if (!state.gCounters.forAll(counter -> counter.query() == TIMES_TO_INCREMENT)) {
            TimeUnit.MICROSECONDS.sleep(100);
          } else {
            break;
          }
        }
      }
      bh.consume(sourceGCounter);
    }
  }

  public static void main(String[] args) throws Exception {
    Options opt =
        new OptionsBuilder()
            .include(PerformanceTest.class.getName())
            .jvmArgsAppend("-XX:-RestrictContended")
            .syncIterations(true)
            .build();

    final Collection<RunResult> runResults = new Runner(opt).run();

    Files.deleteIfExists(Paths.get(RES_FILE_PATH));
    Files.createDirectories(Paths.get(RES_FILE_PATH).getParent());
    Files.createFile(Paths.get(RES_FILE_PATH));
    Files.write(
        Paths.get(RES_FILE_PATH),
        ("Id,"
                + "Mode,"
                + "Cnt,"
                + "Threads,"
                + "Score,"
                + "Error,"
                + "Units"
                + System.lineSeparator())
            .getBytes(),
        StandardOpenOption.APPEND);

    runResults.forEach(
        runResult -> {
          final String id = runResult.getParams().id();
          final Mode mode = runResult.getParams().getMode();
          final long sampleCount = runResult.getPrimaryResult().getSampleCount();
          final int threads = runResult.getParams().getThreads();
          final double score = runResult.getPrimaryResult().getScore();
          final double scoreError = runResult.getPrimaryResult().getScoreError();
          final String scoreUnit = runResult.getPrimaryResult().getScoreUnit();

          try {
            Files.write(
                Paths.get(RES_FILE_PATH),
                (id
                        + ","
                        + mode
                        + ","
                        + sampleCount
                        + ","
                        + threads
                        + ","
                        + score
                        + ","
                        + scoreError
                        + ","
                        + scoreUnit
                        + System.lineSeparator())
                    .getBytes(),
                StandardOpenOption.APPEND);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }
}
