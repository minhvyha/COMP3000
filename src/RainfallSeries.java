import java.util.Objects;

/** Daily rainfall depths in mm, deliberately separate from L/s flow values. */
final class RainfallSeries {
  private final double[] values;

  private RainfallSeries(double[] values) {
    ModelChecks.length(values.length);
    this.values = values.clone();
    for (int day = 0; day < this.values.length; day++) {
      this.values[day] = ModelChecks.nonNegative(this.values[day], "Rainfall on day " + (day + 1));
    }
  }

  static RainfallSeries of(double... values) {
    return new RainfallSeries(Objects.requireNonNull(values, "Daily rainfall"));
  }

  int length() { return values.length; }

  double at(int dayIndex) {
    if (dayIndex < 0) throw new IllegalArgumentException("Day index must be non-negative.");
    return dayIndex < length() ? values[dayIndex] : 0;
  }

  FlowValue runoff(double catchmentAreaM2) {
    ModelChecks.nonNegative(catchmentAreaM2, "Catchment area (m2)");
    double[] result = new double[length()];
    for (int day = 0; day < result.length; day++) {
      result[day] = Units.dailyLitresToFlow(Units.rainfallToLitres(values[day], catchmentAreaM2));
    }
    return FlowValue.of(result);
  }
}
