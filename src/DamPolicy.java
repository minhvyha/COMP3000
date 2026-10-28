import java.util.List;
import java.util.Objects;

/** Ordered, immutable release rules; the first match wins, then the default. */
final class DamPolicy {
  enum Factor { INFLOW, RAIN, LEVEL }
  enum Comparison { LESS, LESS_EQUAL, EQUAL, GREATER_EQUAL, GREATER }

  record Context(double inflowLps, double rainMm, double previousStorageLitres) {
    Context {
      ModelChecks.nonNegative(inflowLps, "Dam inflow (L/s)");
      ModelChecks.nonNegative(rainMm, "Today's rainfall (mm)");
      ModelChecks.nonNegative(previousStorageLitres, "Previous storage (L)");
    }

    double value(Factor factor) {
      return switch (factor) {
        case INFLOW -> inflowLps;
        case RAIN -> rainMm;
        case LEVEL -> previousStorageLitres;
      };
    }
  }

  record Rule(Factor factor, Comparison comparison, double threshold, double releaseFraction) {
    Rule {
      Objects.requireNonNull(factor, "Rule factor");
      Objects.requireNonNull(comparison, "Rule comparison");
      ModelChecks.nonNegative(threshold, "Rule threshold");
      ModelChecks.fraction(releaseFraction, "Release fraction");
    }

    boolean matches(Context context) {
      double actual = context.value(factor);
      return switch (comparison) {
        case LESS -> actual < threshold;
        case LESS_EQUAL -> actual <= threshold;
        case EQUAL -> actual == threshold;
        case GREATER_EQUAL -> actual >= threshold;
        case GREATER -> actual > threshold;
      };
    }
  }

  // -1 is the default; other indices refer to the original rule list.
  record Decision(int ruleIndex, double releaseFraction) {
    Decision { ModelChecks.fraction(releaseFraction, "Release fraction"); }

    String description() { return ruleIndex == -1 ? "default" : "rule " + (ruleIndex + 1); }
  }

  private final List<Rule> rules;
  private final double defaultFraction;

  DamPolicy(List<Rule> rules, double defaultFraction) {
    this.rules = List.copyOf(Objects.requireNonNull(rules, "Dam rules"));
    this.defaultFraction = ModelChecks.fraction(defaultFraction, "Default release fraction");
  }

  static DamPolicy fixed(double releaseFraction) {
    return new DamPolicy(List.of(), releaseFraction);
  }

  Decision choose(Context context) {
    Objects.requireNonNull(context, "Dam context");
    for (int index = 0; index < rules.size(); index++) {
      if (rules.get(index).matches(context)) return new Decision(index, rules.get(index).releaseFraction());
    }
    return new Decision(-1, defaultFraction);
  }
}
