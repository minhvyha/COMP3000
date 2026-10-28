import java.util.Arrays;
import java.util.Objects;

/** An immutable sequence of non-negative daily average flows in L/s. */
final class FlowValue {
  // Pending entries begin on the first day after the requested reporting window.
  record Window(FlowValue withinWindow, FlowValue pending) {}

  private final double[] values;

  private FlowValue(double[] values) {
    ModelChecks.length(values.length);
    this.values = values.clone();
    for (int day = 0; day < this.values.length; day++) {
      this.values[day] = ModelChecks.nonNegative(this.values[day], "Flow on day " + (day + 1));
    }
  }

  static FlowValue of(double... values) {
    return new FlowValue(Objects.requireNonNull(values, "Daily flows"));
  }

  int length() { return values.length; }

  // Java indices are zero-based; reporting uses index + 1. Outside the tail is dry.
  double at(int dayIndex) {
    if (dayIndex < 0) throw new IllegalArgumentException("Day index must be non-negative.");
    return dayIndex < values.length ? values[dayIndex] : 0;
  }

  double[] toArray() { return values.clone(); }

  double totalVolumeLitres() {
    return Units.flowToDailyLitres(ModelChecks.sum(values, "Total flow"));
  }

  Window splitAt(double dayCount) {
    int count = ModelChecks.days(dayCount, true, "Window days");
    int boundary = Math.min(count, length());
    return new Window(of(Arrays.copyOfRange(values, 0, boundary)),
        of(Arrays.copyOfRange(values, boundary, length())));
  }

  FlowValue add(FlowValue other) {
    Objects.requireNonNull(other, "Other flow");
    double[] result = new double[Math.max(length(), other.length())];
    for (int day = 0; day < result.length; day++) result[day] = at(day) + other.at(day);
    return of(result);
  }

  FlowValue subtract(FlowValue other) {
    Objects.requireNonNull(other, "Other flow");
    double[] result = new double[Math.max(length(), other.length())];
    for (int day = 0; day < result.length; day++) result[day] = at(day) - other.at(day);
    return of(result);
  }

  FlowValue multiply(double scalar) {
    ModelChecks.nonNegative(scalar, "Flow multiplier");
    double[] result = new double[length()];
    for (int day = 0; day < result.length; day++) result[day] = values[day] * scalar;
    return of(result);
  }

  FlowValue divide(double scalar) {
    ModelChecks.nonNegative(scalar, "Flow divisor");
    if (scalar == 0) throw new IllegalArgumentException("Flow divisor must be greater than zero.");
    double[] result = new double[length()];
    for (int day = 0; day < result.length; day++) result[day] = values[day] / scalar;
    return of(result);
  }

  FlowValue delay(double days) {
    int offset = ModelChecks.days(days, true, "Delay days");
    if (length() == 0 || offset == 0) return this;
    double[] result = new double[ModelChecks.length((long) length() + offset)];
    System.arraycopy(values, 0, result, offset, length());
    return of(result);
  }

  FlowValue retain(double heldFraction) {
    ModelChecks.fraction(heldFraction, "Held fraction");
    if (length() == 0 || heldFraction == 0) return this;
    double[] result = new double[ModelChecks.length((long) length() + 1)];
    for (int day = 0; day < result.length; day++) {
      double current = at(day);
      double previous = day == 0 ? 0 : at(day - 1);
      // Last day's held water is released in full; it is not retained again.
      result[day] = (1 - heldFraction) * current + heldFraction * previous;
    }
    return of(result);
  }

  FlowValue spread(double days) {
    int width = ModelChecks.days(days, false, "Spread days");
    if (length() == 0 || width == 1) return this;
    int resultLength = ModelChecks.length((long) length() + width - 1);
    if ((long) length() * width > ModelChecks.MAX_SPREAD_CONTRIBUTIONS) {
      throw new IllegalArgumentException("Spread exceeds the limit of "
          + ModelChecks.MAX_SPREAD_CONTRIBUTIONS + " contributions.");
    }
    double[] result = new double[resultLength];
    for (int day = 0; day < length(); day++) {
      double contribution = values[day] / width;
      for (int offset = 0; offset < width; offset++) result[day + offset] += contribution;
    }
    return of(result);
  }

  @Override public String toString() { return Arrays.toString(values); }
}
