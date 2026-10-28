import java.util.List;
import java.util.Locale;

/** Demonstrates the Java domain model before its integration into RiverLox. */
public final class SemanticsDemo {
  private SemanticsDemo() {}

  public static void main(String[] args) {
    RainfallSeries rain = RainfallSeries.of(1, 0, 0);
    System.out.println("Rainfall [1, 0, 0] mm; catchment 864000 m2:");
    System.out.println("Runoff (L/s): " + rain.runoff(864_000));
    System.out.println("delay([8, 4], 1): " + FlowValue.of(8, 4).delay(1));
    System.out.println("retain([8, 4], 0.25): " + FlowValue.of(8, 4).retain(0.25));
    System.out.println("spread([6, 3], 3): " + FlowValue.of(6, 3).spread(3));
    FlowValue pulse = FlowValue.of(8, 0, 0);
    for (double fraction : new double[] {0, 0.5, 1}) {
      DamModel.Result result = DamModel.simulate(pulse, RainfallSeries.of(), DamPolicy.fixed(fraction), 3);
      System.out.println("Release fraction " + fraction + ": " + result.outflow()
          + " L/s; final storage " + result.finalStorageLitres() + " L");
    }

    DamPolicy adaptive = new DamPolicy(List.of(
        new DamPolicy.Rule(DamPolicy.Factor.RAIN, DamPolicy.Comparison.GREATER_EQUAL, 5, 0.25),
        new DamPolicy.Rule(DamPolicy.Factor.LEVEL, DamPolicy.Comparison.GREATER_EQUAL, 300_000, 0.75)), 0.5);
    DamModel.Result result = DamModel.simulate(pulse, RainfallSeries.of(6, 0, 0), adaptive, 3);
    System.out.println("Adaptive dam trace:");
    System.out.println("Day | Inflow L/s | Rain mm | Start L | Rule | Fraction | Outflow L/s | End L");
    for (DamModel.Day day : result.days()) {
      System.out.printf(Locale.ROOT, "%d | %.3f | %.1f | %.1f | %s | %.2f | %.3f | %.1f%n",
          day.number(), day.inflowLps(), day.rainMm(), day.startingStorageLitres(),
          day.decision().description(), day.decision().releaseFraction(), day.outflowLps(),
          day.endingStorageLitres());
    }
    System.out.println("The .river entry point remains the A1 parser at this milestone.");
  }
}
