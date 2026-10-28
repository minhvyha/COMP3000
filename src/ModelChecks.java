/** Shared numeric and resource limits for the daily water model. */
final class ModelChecks {
  static final int MAX_DAYS = 100_000;
  static final long MAX_SPREAD_CONTRIBUTIONS = 5_000_000;

  private ModelChecks() {}

  static double nonNegative(double value, String name) {
    if (!Double.isFinite(value) || value < 0) {
      throw new IllegalArgumentException(name + " must be finite and non-negative.");
    }
    return value == 0 ? 0 : value;
  }

  static double fraction(double value, String name) {
    nonNegative(value, name);
    if (value > 1) throw new IllegalArgumentException(name + " must not exceed 1.");
    return value;
  }

  static int days(double value, boolean allowZero, String name) {
    nonNegative(value, name);
    if (value != Math.rint(value) || value > MAX_DAYS || (!allowZero && value == 0)) {
      throw new IllegalArgumentException(name + " must be a whole number between "
          + (allowZero ? 0 : 1) + " and " + MAX_DAYS + ".");
    }
    return (int) value;
  }

  static int length(long length) {
    if (length < 0 || length > MAX_DAYS) {
      throw new IllegalArgumentException("A daily sequence may have at most " + MAX_DAYS + " entries.");
    }
    return (int) length;
  }

  // Compensated summation keeps small daily contributions from being needlessly lost.
  static double sum(double[] values, String name) {
    double total = 0;
    double correction = 0;
    for (double value : values) {
      double adjusted = value - correction;
      double next = nonNegative(total + adjusted, name);
      correction = (next - total) - adjusted;
      total = next;
    }
    return total;
  }
}
