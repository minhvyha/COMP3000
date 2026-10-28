/** Units are daily average L/s, daily rainfall mm, area m2, and stored litres. */
final class Units {
  static final double SECONDS_PER_DAY = 86_400;

  private Units() {}

  static double rainfallToLitres(double rainMm, double catchmentAreaM2) {
    ModelChecks.nonNegative(rainMm, "Rainfall (mm)");
    ModelChecks.nonNegative(catchmentAreaM2, "Catchment area (m2)");
    // A millimetre of rain over a square metre is one litre.
    return ModelChecks.nonNegative(rainMm * catchmentAreaM2, "Rainfall volume (L)");
  }

  static double flowToDailyLitres(double flowLps) {
    ModelChecks.nonNegative(flowLps, "Flow (L/s)");
    return ModelChecks.nonNegative(flowLps * SECONDS_PER_DAY, "Daily volume (L)");
  }

  static double dailyLitresToFlow(double litres) {
    ModelChecks.nonNegative(litres, "Daily volume (L)");
    return litres / SECONDS_PER_DAY;
  }
}
