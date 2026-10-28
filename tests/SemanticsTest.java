import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Independent numeric fixtures and conservation checks; no -ea switch required. */
public final class SemanticsTest {
  private static int groups;
  private static int checks;

  private SemanticsTest() {}

  public static void main(String[] args) {
    run("unit conversion", () -> {
      close(Units.rainfallToLitres(1, 1), 1);
      close(Units.rainfallToLitres(1, 864_000), 864_000);
      close(Units.flowToDailyLitres(8), 691_200);
      close(Units.dailyLitresToFlow(691_200), 8);
    });
    run("rain and area independently scale runoff", () -> {
      flow(RainfallSeries.of(1, 0, 0).runoff(864_000), 10, 0, 0);
      flow(RainfallSeries.of(2, 0, 0).runoff(864_000), 20, 0, 0);
      flow(RainfallSeries.of(1, 0, 0).runoff(1_728_000), 20, 0, 0);
      close(RainfallSeries.of(1, 0, 0).runoff(864_000).totalVolumeLitres(), 864_000);
      flow(RainfallSeries.of(1, 3).runoff(0), 0, 0);
    });
    run("rainfall is copied and zero-padded", () -> {
      double[] values = {1, 2};
      RainfallSeries rain = RainfallSeries.of(values);
      values[0] = 99;
      close(rain.at(0), 1);
      close(rain.at(2), 0);
      invalid(() -> rain.at(-1));
      flow(RainfallSeries.of().runoff(1));
    });
    run("invalid units and rainfall fail", () -> {
      for (double invalid : new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY}) {
        invalid(() -> RainfallSeries.of(invalid));
        invalid(() -> Units.rainfallToLitres(invalid, 1));
        invalid(() -> Units.rainfallToLitres(1, invalid));
        invalid(() -> Units.flowToDailyLitres(invalid));
        invalid(() -> Units.dailyLitresToFlow(invalid));
      }
      invalid(() -> RainfallSeries.of().runoff(-1));
      invalid(() -> Units.rainfallToLitres(Double.MAX_VALUE, 2));
      invalid(() -> Units.flowToDailyLitres(Double.MAX_VALUE));
    });
    run("immutable flow values", () -> {
      double[] values = {8, 4};
      FlowValue original = FlowValue.of(values);
      values[0] = 99;
      double[] exported = original.toArray();
      exported[1] = 99;
      original.add(FlowValue.of(1));
      original.retain(0.25);
      original.spread(3);
      flow(original, 8, 4);
      close(original.at(2), 0);
      invalid(() -> original.at(-1));
    });
    run("zero-padded addition and subtraction", () -> {
      flow(FlowValue.of(1, 2).add(FlowValue.of(3)), 4, 2);
      flow(FlowValue.of(4, 2).subtract(FlowValue.of(3)), 1, 2);
      flow(FlowValue.of().add(FlowValue.of(3)), 3);
      invalid(() -> FlowValue.of(1).subtract(FlowValue.of(2)));
    });
    run("scalar flow arithmetic", () -> {
      flow(FlowValue.of(8, 4).multiply(0.5), 4, 2);
      flow(FlowValue.of(8, 4).divide(2), 4, 2);
      flow(FlowValue.of(8, 4).multiply(0), 0, 0);
      invalid(() -> FlowValue.of(8).divide(0));
      invalid(() -> FlowValue.of(8).divide(-1));
      invalid(() -> FlowValue.of(8).multiply(-1));
    });
    run("physical flows reject non-finite and negative results", () -> {
      invalid(() -> FlowValue.of(-1));
      invalid(() -> FlowValue.of(Double.NaN));
      invalid(() -> FlowValue.of(Double.POSITIVE_INFINITY));
      invalid(() -> FlowValue.of(Double.MAX_VALUE).add(FlowValue.of(Double.MAX_VALUE)));
      invalid(() -> FlowValue.of(1).divide(Double.MIN_VALUE));
      invalid(() -> FlowValue.of(1).multiply(Double.NaN));
      invalid(() -> FlowValue.of(Double.MAX_VALUE).totalVolumeLitres());
    });
    run("delay retains its whole tail", () -> {
      flow(FlowValue.of(8, 4).delay(1), 0, 8, 4);
      flow(FlowValue.of(8, 4).delay(0), 8, 4);
      FlowValue late = FlowValue.of(8).delay(12);
      equal(late.length(), 13);
      close(late.at(12), 8);
      close(late.totalVolumeLitres(), 691_200);
    });
    run("retain releases previously held water in full", () -> {
      flow(FlowValue.of(8, 4).retain(0.25), 6, 5, 1);
      flow(FlowValue.of(8, 4).retain(0), 8, 4);
      flow(FlowValue.of(8, 4).retain(1), 0, 8, 4);
      flow(RainfallSeries.of(1, 0, 0).runoff(864_000).retain(0.5), 5, 5, 0, 0);
    });
    run("spread releases a finite equal-share tail", () -> {
      flow(FlowValue.of(6, 3).spread(3), 2, 3, 3, 1);
      flow(FlowValue.of(6, 3).spread(1), 6, 3);
      close(FlowValue.of(6, 3).spread(3).totalVolumeLitres(), 777_600);
    });
    run("empty flow operations stay empty", () -> {
      flow(FlowValue.of().delay(10));
      flow(FlowValue.of().retain(1));
      flow(FlowValue.of().spread(10));
      close(FlowValue.of().totalVolumeLitres(), 0);
      invalid(() -> FlowValue.of().retain(1.1));
      invalid(() -> FlowValue.of().spread(0));
    });
    run("routing parameters and bounded work", () -> {
      for (double invalid : new double[] {-1, 1.5, Double.NaN, Double.POSITIVE_INFINITY}) {
        invalid(() -> FlowValue.of(1).delay(invalid));
        invalid(() -> FlowValue.of(1).spread(invalid));
      }
      invalid(() -> FlowValue.of(1).spread(0));
      invalid(() -> FlowValue.of(1).retain(-0.1));
      invalid(() -> FlowValue.of(1).retain(1.1));
      invalid(() -> FlowValue.of(1).retain(Double.NaN));
      invalid(() -> FlowValue.of(1).delay(ModelChecks.MAX_DAYS));
      invalid(() -> FlowValue.of(new double[ModelChecks.MAX_DAYS + 1]));
      invalid(() -> RainfallSeries.of(new double[ModelChecks.MAX_DAYS + 1]));
      invalid(() -> FlowValue.of(new double[3_000]).spread(2_000));
    });
    run("reporting windows preserve pending water", () -> {
      FlowValue original = FlowValue.of(2, 3, 5);
      FlowValue.Window window = original.splitAt(2);
      flow(window.withinWindow(), 2, 3);
      flow(window.pending(), 5);
      close(window.withinWindow().totalVolumeLitres() + window.pending().totalVolumeLitres(),
          original.totalVolumeLitres());
      flow(original.splitAt(0).pending(), 2, 3, 5);
      flow(original.splitAt(10).pending());
      flow(FlowValue.of().splitAt(3).withinWindow());
      invalid(() -> original.splitAt(-1));
      invalid(() -> original.splitAt(1.5));
    });
    run("closed dam stores all incoming water", () -> {
      DamModel.Result result = pulseDam(0, 3);
      flow(result.outflow(), 0, 0, 0);
      close(result.finalStorageLitres(), 691_200);
      balanced(result, FlowValue.of(8, 0, 0));
    });
    run("half dam releases stored water on dry days", () -> {
      DamModel.Result result = pulseDam(0.5, 3);
      flow(result.outflow(), 4, 2, 1);
      close(result.days().get(0).endingStorageLitres(), 345_600);
      close(result.days().get(1).endingStorageLitres(), 172_800);
      close(result.days().get(2).endingStorageLitres(), 86_400);
      balanced(result, FlowValue.of(8, 0, 0));
    });
    run("open dam passes all flow", () -> {
      DamModel.Result result = pulseDam(1, 3);
      flow(result.outflow(), 8, 0, 0);
      close(result.finalStorageLitres(), 0);
      balanced(result, FlowValue.of(8, 0, 0));
    });
    run("extended finite horizon reports leftover storage", () -> {
      DamModel.Result result = DamModel.simulate(FlowValue.of(8), RainfallSeries.of(), DamPolicy.fixed(0.5), 5);
      flow(result.outflow(), 4, 2, 1, 0.5, 0.25);
      close(result.finalStorageLitres(), 21_600);
      balanced(result, FlowValue.of(8));
    });
    run("invalid fractions and rule thresholds fail before execution", () -> {
      for (double invalid : new double[] {-0.1, 1.01, Double.NaN, Double.POSITIVE_INFINITY}) {
        invalid(() -> DamPolicy.fixed(invalid));
        invalid(() -> rule(DamPolicy.Factor.RAIN, 5, invalid));
      }
      invalid(() -> rule(DamPolicy.Factor.LEVEL, -1, 0.5));
      invalid(() -> rule(DamPolicy.Factor.INFLOW, Double.NaN, 0.5));
    });
    run("first matching rule wins and default is used", () -> {
      DamPolicy policy = adaptive();
      DamPolicy.Decision both = policy.choose(new DamPolicy.Context(8, 5, 300_000));
      equal(both.ruleIndex(), 0);
      close(both.releaseFraction(), 0.25);
      DamPolicy.Decision neither = policy.choose(new DamPolicy.Context(8, 0, 0));
      equal(neither.ruleIndex(), -1);
      close(neither.releaseFraction(), 0.5);
    });
    run("rain and level independently select different fractions", () -> {
      DamPolicy policy = adaptive();
      close(policy.choose(new DamPolicy.Context(8, 4.99, 100_000)).releaseFraction(), 0.5);
      close(policy.choose(new DamPolicy.Context(8, 5, 100_000)).releaseFraction(), 0.25);
      close(policy.choose(new DamPolicy.Context(8, 5.01, 100_000)).releaseFraction(), 0.25);
      close(policy.choose(new DamPolicy.Context(8, 0, 299_999)).releaseFraction(), 0.5);
      close(policy.choose(new DamPolicy.Context(8, 0, 300_000)).releaseFraction(), 0.75);
      close(policy.choose(new DamPolicy.Context(8, 0, 300_001)).releaseFraction(), 0.75);
    });
    run("inflow can also drive a policy", () -> {
      DamPolicy policy = new DamPolicy(List.of(rule(DamPolicy.Factor.INFLOW, 10, 0.75)), 0.1);
      close(policy.choose(new DamPolicy.Context(9.99, 0, 0)).releaseFraction(), 0.1);
      close(policy.choose(new DamPolicy.Context(10, 0, 0)).releaseFraction(), 0.75);
    });
    run("comparison boundary meanings", () -> {
      DamPolicy.Context context = new DamPolicy.Context(8, 5, 0);
      check(new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.LESS, 6, 0).matches(context), "less");
      check(!new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.LESS, 5, 0).matches(context), "strict less");
      check(new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.LESS_EQUAL, 5, 0).matches(context), "less equal");
      check(new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.EQUAL, 5, 0).matches(context), "equal");
      check(new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.GREATER_EQUAL, 5, 0).matches(context), "greater equal");
      check(new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.GREATER, 4, 0).matches(context), "greater");
      check(!new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.GREATER, 5, 0).matches(context), "strict greater");
    });
    run("level means previous ending storage", () -> {
      DamPolicy policy = new DamPolicy(List.of(rule(DamPolicy.Factor.LEVEL, 600_000, 1)), 0);
      DamModel.Result result = DamModel.simulate(FlowValue.of(8, 0, 0), RainfallSeries.of(), policy, 3);
      flow(result.outflow(), 0, 8, 0);
      equal(result.days().get(0).decision().ruleIndex(), -1);
      equal(result.days().get(1).decision().ruleIndex(), 0);
      close(result.finalStorageLitres(), 0);
    });
    run("adaptive trace has hand-calculated values", () -> {
      DamModel.Result result = DamModel.simulate(FlowValue.of(8, 0, 0), RainfallSeries.of(6, 0, 0), adaptive(), 3);
      flow(result.outflow(), 2, 4.5, 0.75);
      close(result.days().get(0).endingStorageLitres(), 518_400);
      close(result.days().get(1).endingStorageLitres(), 129_600);
      close(result.finalStorageLitres(), 64_800);
      equal(result.days().get(0).decision().ruleIndex(), 0);
      equal(result.days().get(1).decision().ruleIndex(), 1);
      equal(result.days().get(2).decision().ruleIndex(), -1);
      balanced(result, FlowValue.of(8, 0, 0));
    });
    run("short horizons fail instead of discarding input", () -> {
      invalid(() -> DamModel.simulate(FlowValue.of(8, 0), RainfallSeries.of(), DamPolicy.fixed(0.5), 1));
      invalid(() -> DamModel.simulate(FlowValue.of(), RainfallSeries.of(1, 0), DamPolicy.fixed(0.5), 1));
      invalid(() -> DamModel.simulate(FlowValue.of(), RainfallSeries.of(), DamPolicy.fixed(0.5), 0));
      invalid(() -> DamModel.simulate(FlowValue.of(), RainfallSeries.of(), DamPolicy.fixed(0.5), -1));
      invalid(() -> DamModel.simulate(FlowValue.of(), RainfallSeries.of(), DamPolicy.fixed(0.5), ModelChecks.MAX_DAYS + 1));
      invalid(() -> DamModel.simulate(FlowValue.of(Double.MAX_VALUE), RainfallSeries.of(), DamPolicy.fixed(1), 1));
    });
    run("independent dams and repeated runs have no leaked state", () -> {
      DamPolicy policy = DamPolicy.fixed(0.5);
      DamModel.Result first = DamModel.simulate(FlowValue.of(8, 0, 0), RainfallSeries.of(), policy, 3);
      DamModel.simulate(FlowValue.of(20, 0, 0), RainfallSeries.of(), policy, 3);
      DamModel.Result repeated = DamModel.simulate(FlowValue.of(8, 0, 0), RainfallSeries.of(), policy, 3);
      flow(repeated.outflow(), first.outflow().toArray());
      close(first.finalStorageLitres(), repeated.finalStorageLitres());
      List<DamPolicy.Rule> editable = new ArrayList<>();
      editable.add(rule(DamPolicy.Factor.RAIN, 5, 0.25));
      DamPolicy copied = new DamPolicy(editable, 0.5);
      editable.clear();
      close(copied.choose(new DamPolicy.Context(8, 5, 0)).releaseFraction(), 0.25);
      boolean blocked = false;
      try { first.days().clear(); } catch (UnsupportedOperationException expected) { blocked = true; }
      check(blocked, "Trace must be immutable");
    });
    run("downstream rivers receive released flow", () -> {
      FlowValue tributary = FlowValue.of(1, 1, 1);
      flow(pulseDam(0, 3).outflow().add(tributary), 1, 1, 1);
      flow(pulseDam(0.5, 3).outflow().add(tributary), 5, 3, 2);
      flow(pulseDam(1, 3).outflow().add(tributary), 9, 1, 1);
    });
    run("two dams and travel delay preserve all water", () -> {
      int horizon = 5;
      FlowValue source = FlowValue.of(8);
      DamModel.Result upper = DamModel.simulate(source, RainfallSeries.of(), DamPolicy.fixed(0.5), horizon);
      FlowValue.Window routed = upper.outflow().delay(1).splitAt(horizon);
      // The final released daily amount is still in transit at the shared endpoint.
      double inTransit = routed.pending().totalVolumeLitres();
      DamModel.Result lower = DamModel.simulate(routed.withinWindow(), RainfallSeries.of(), DamPolicy.fixed(0.5), horizon);
      close(lower.outflow().totalVolumeLitres() + lower.finalStorageLitres()
          + upper.finalStorageLitres() + inTransit, source.totalVolumeLitres());
      close(inTransit, 21_600);
    });
    run("seeded conservation checks for routing and dams", () -> {
      Random random = new Random(3000);
      for (int trial = 0; trial < 100; trial++) {
        double[] values = new double[1 + random.nextInt(15)];
        for (int day = 0; day < values.length; day++) values[day] = random.nextDouble() * 1_000;
        FlowValue input = FlowValue.of(values);
        double volume = input.totalVolumeLitres();
        close(input.delay(random.nextInt(5)).totalVolumeLitres(), volume);
        close(input.retain(random.nextDouble()).totalVolumeLitres(), volume);
        close(input.spread(1 + random.nextInt(5)).totalVolumeLitres(), volume);
        DamModel.Result result = DamModel.simulate(input, RainfallSeries.of(), DamPolicy.fixed(random.nextDouble()), input.length() + 3);
        balanced(result, input);
      }
    });
    System.out.println("PASS " + groups + " semantic test groups (" + checks + " checks).");
  }

  private static DamPolicy.Rule rule(DamPolicy.Factor factor, double threshold, double fraction) {
    return new DamPolicy.Rule(factor, DamPolicy.Comparison.GREATER_EQUAL, threshold, fraction);
  }

  private static DamPolicy adaptive() {
    return new DamPolicy(List.of(rule(DamPolicy.Factor.RAIN, 5, 0.25),
        rule(DamPolicy.Factor.LEVEL, 300_000, 0.75)), 0.5);
  }

  private static DamModel.Result pulseDam(double fraction, int days) {
    return DamModel.simulate(FlowValue.of(8, 0, 0), RainfallSeries.of(), DamPolicy.fixed(fraction), days);
  }

  private static void balanced(DamModel.Result result, FlowValue input) {
    close(input.totalVolumeLitres(), result.outflow().totalVolumeLitres() + result.finalStorageLitres());
    double previous = 0;
    for (DamModel.Day day : result.days()) {
      close(day.startingStorageLitres(), previous);
      close(day.availableLitres(), day.startingStorageLitres() + Units.flowToDailyLitres(day.inflowLps()));
      close(day.availableLitres(), day.releasedLitres() + day.endingStorageLitres());
      check(day.endingStorageLitres() >= 0, "Negative storage");
      check(day.releasedLitres() <= day.availableLitres(), "Released unavailable water");
      previous = day.endingStorageLitres();
    }
  }

  private static void run(String name, Runnable test) {
    try { test.run(); groups++; }
    catch (Throwable failure) { throw new AssertionError("Failed: " + name, failure); }
  }

  private static void flow(FlowValue actual, double... expected) {
    equal(actual.length(), expected.length);
    for (int day = 0; day < expected.length; day++) close(actual.at(day), expected[day]);
  }

  private static void close(double actual, double expected) {
    // Absolute 1e-6 in the tested unit plus relative 1e-10.
    check(Double.isFinite(actual) && Math.abs(actual - expected)
        <= 1e-6 + 1e-10 * Math.max(Math.abs(actual), Math.abs(expected)),
        "Expected " + expected + ", received " + actual);
  }

  private static void equal(int actual, int expected) {
    check(actual == expected, "Expected " + expected + ", received " + actual);
  }

  private static void invalid(Runnable action) {
    boolean failed = false;
    try { action.run(); }
    catch (IllegalArgumentException expected) { failed = true; }
    check(failed, "Expected invalid input to be rejected");
  }

  private static void check(boolean passed, String message) {
    checks++;
    if (!passed) throw new AssertionError(message);
  }
}
