package io.cdap.wrangler.plugin.directives.aggregate;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveContext;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A custom directive that aggregates byte size and time duration columns.
 */
public class AggregateStatsDirective implements Directive {

    private String byteSourceCol;
    private String timeSourceCol;
    private String byteTargetCol;
    private String timeTargetCol;

    private String byteUnit = "MB";     // default output
    private String timeUnit = "seconds";
    private boolean average = false;

    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder("aggregate-stats")
                .addRequiredArg("byteSourceCol", TokenType.COLUMN_NAME)
                .addRequiredArg("timeSourceCol", TokenType.COLUMN_NAME)
                .addRequiredArg("byteTargetCol", TokenType.IDENTIFIER)
                .addRequiredArg("timeTargetCol", TokenType.IDENTIFIER)
                .addOptionalArg("byteUnit", TokenType.TEXT)
                .addOptionalArg("timeUnit", TokenType.TEXT)
                .addOptionalArg("average", TokenType.BOOLEAN)
                .build();
    }

    @Override
    public void initialize(Arguments arguments) {
        this.byteSourceCol = arguments.value("byteSourceCol");
        this.timeSourceCol = arguments.value("timeSourceCol");
        this.byteTargetCol = arguments.value("byteTargetCol");
        this.timeTargetCol = arguments.value("timeTargetCol");

        if (arguments.contains("byteUnit")) {
            this.byteUnit = arguments.value("byteUnit").toUpperCase();
        }
        if (arguments.contains("timeUnit")) {
            this.timeUnit = arguments.value("timeUnit").toLowerCase();
        }
        if (arguments.contains("average")) {
            this.average = Boolean.parseBoolean(arguments.value("average"));
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) {
        long totalBytes = 0L;
        long totalTimeMs = 0L;
        int count = 0;

        for (Row row : rows) {
            try {
                String byteValue = row.getValue(byteSourceCol).toString();
                String timeValue = row.getValue(timeSourceCol).toString();

                ByteSize size = new ByteSize(byteValue);
                TimeDuration time = new TimeDuration(timeValue);

                totalBytes += size.getBytes();
                totalTimeMs += time.getMilliseconds();
                count++;
            } catch (Exception e) {
                // Optional: log or handle malformed row
            }
        }

        double resultBytes = convertBytes(totalBytes, byteUnit);
        double resultTime = convertTime(totalTimeMs, timeUnit);

        if (average && count > 0) {
            resultBytes /= count;
            resultTime /= count;
        }

        Row result = new Row();
        result.add(byteTargetCol, resultBytes);
        result.add(timeTargetCol, resultTime);

        return Collections.singletonList(result);
    }

    private double convertBytes(long bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "KB": return bytes / 1024.0;
            case "MB": return bytes / (1024.0 * 1024.0);
            case "GB": return bytes / (1024.0 * 1024.0 * 1024.0);
            default: return bytes;
        }
    }

    private double convertTime(long ms, String unit) {
        switch (unit.toLowerCase()) {
            case "seconds": return ms / 1000.0;
            case "minutes": return ms / (60.0 * 1000.0);
            default: return ms;
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    @Override
    public void initialize(DirectiveContext context) {
        // no-op for this directive
    }
}
