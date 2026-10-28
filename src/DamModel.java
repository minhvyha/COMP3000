import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Finite daily dam simulation, starting empty, with all state local to one run. */
final class DamModel {
  record Day(int number, double inflowLps, double rainMm, double startingStorageLitres,
      double availableLitres, DamPolicy.Decision decision, double releasedLitres,
      double outflowLps, double endingStorageLitres) {}

  record Result(FlowValue outflow, List<Day> days, double finalStorageLitres) {
    Result { days = List.copyOf(days); }
  }

  private DamModel() {}

  static Result simulate(FlowValue inflow, RainfallSeries rain, DamPolicy policy, int dayCount) {
    Objects.requireNonNull(inflow, "Dam inflow");
    Objects.requireNonNull(rain, "Daily rainfall");
    Objects.requireNonNull(policy, "Dam policy");
    ModelChecks.days(dayCount, false, "Simulation days");
    if (dayCount < inflow.length() || dayCount < rain.length()) {
      throw new IllegalArgumentException("Simulation days must cover the complete inflow and rainfall sequences.");
    }

    double storage = 0;
    double[] output = new double[dayCount];
    List<Day> trace = new ArrayList<>(dayCount);
    for (int index = 0; index < dayCount; index++) {
      double todayInflow = inflow.at(index);
      double todayRain = rain.at(index);
      double startingStorage = storage;
      DamPolicy.Context context = new DamPolicy.Context(todayInflow, todayRain, startingStorage);
      DamPolicy.Decision decision = policy.choose(context);
      double available = ModelChecks.nonNegative(
          startingStorage + Units.flowToDailyLitres(todayInflow), "Available water (L)");
      double released = available * decision.releaseFraction();
      storage = ModelChecks.nonNegative(available - released, "Ending storage (L)");
      output[index] = Units.dailyLitresToFlow(released);
      trace.add(new Day(index + 1, todayInflow, todayRain, startingStorage, available,
          decision, released, output[index], storage));
    }
    return new Result(FlowValue.of(output), trace, storage);
  }
}
