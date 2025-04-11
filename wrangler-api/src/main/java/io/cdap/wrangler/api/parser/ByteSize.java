package io.cdap.wrangler.api.parser;

/**
 * A custom Token parser for ByteSize values like "10KB", "1.5MB", "2GB".
 * Converts to bytes using base 1024.
 */
public class ByteSize extends Token {
  private final long bytes;

  public ByteSize(String value) {
    super(value);
    this.bytes = parseToBytes(value);
  }

  private long parseToBytes(String value) {
    value = value.trim().toUpperCase();

    // Remove optional trailing 'B' if present (e.g., "10KB" -> "10K")
    if (value.endsWith("B")) {
      value = value.substring(0, value.length() - 1);
    }

    try {
      if (value.endsWith("K")) {
        return (long) (Double.parseDouble(value.replace("K", "")) * 1024);
      } else if (value.endsWith("M")) {
        return (long) (Double.parseDouble(value.replace("M", "")) * 1024 * 1024);
      } else if (value.endsWith("G")) {
        return (long) (Double.parseDouble(value.replace("G", "")) * 1024 * 1024 * 1024);
      } else {
        // fallback: treat it as raw bytes
        return Long.parseLong(value);
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid byte size format: " + value, e);
    }
  }

  /**
   * Returns the size in bytes.
   */
  public long getBytes() {
    return this.bytes;
  }

  @Override
  public String toString() {
    return bytes + " bytes";
  }
}
