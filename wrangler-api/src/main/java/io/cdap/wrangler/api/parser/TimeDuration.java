package io.cdap.wrangler.api.parser;

public class TimeDuration extends Token {
  private final long milliseconds;

  public TimeDuration(String value) {
    super(value);
    this.milliseconds = parseToMilliseconds(value);
  }

  private long parseToMilliseconds(String value) {
    value = value.trim().toLowerCase();

    try {
      if (value.endsWith("ms")) {
        return (long) Double.parseDouble(value.replace("ms", ""));
      } else if (value.endsWith("s") || value.endsWith("sec") || value.endsWith("seconds")) {
        value = value.replace("sec", "").replace("seconds", "").replace("s", "");
        return (long) (Double.parseDouble(value) * 1000);
      } else if (value.endsWith("m") || value.endsWith("min") || value.endsWith("minutes")) {
        value = value.replace("min", "").replace("minutes", "").replace("m", "");
        return (long) (Double.parseDouble(value) * 60 * 1000);
      } else {
        // fallback to assume it's raw ms
        return Long.parseLong(value);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid time duration format: " + value);
    }
  }

  public long getMilliseconds() {
    return this.milliseconds;
  }

  @Override
  public String toString() {
    return Long.toString(milliseconds) + " ms";
  }
}
