package io.cdap.wrangler.plugin.directives.aggregate;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AggregateStatsDirectiveTest {

    @Test
    public void testTotalAggregationInMBandSeconds() throws Exception {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row("data_transfer_size", "1MB").add("response_time", "1000ms"));
        rows.add(new Row("data_transfer_size", "512KB").add("response_time", "500ms"));
        rows.add(new Row("data_transfer_size", "2MB").add("response_time", "1500ms"));

        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
        };

        List<Row> results = TestingRig.execute(recipe, rows);

        Assert.assertEquals(1, results.size());

        Row result = results.get(0);

        // 1MB + 0.5MB + 2MB = 3.5MB
        double expectedMB = 3.5;
        double actualMB = (double) result.getValue("total_size_mb");

        // 1 + 0.5 + 1.5 = 3.0 sec
        double expectedSec = 3.0;
        double actualSec = (double) result.getValue("total_time_sec");

        Assert.assertEquals(expectedMB, actualMB, 0.001);
        Assert.assertEquals(expectedSec, actualSec, 0.001);
    }

    @Test
    public void testAverageAggregation() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("data_transfer_size", "2MB").add("response_time", "2s"),
                new Row("data_transfer_size", "4MB").add("response_time", "4s")
        );

        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time avg_size avg_time average true"
        };

        List<Row> results = TestingRig.execute(recipe, rows);

        Row result = results.get(0);

        // average: (2MB + 4MB) / 2 = 3MB
        Assert.assertEquals(3.0, (double) result.getValue("avg_size"), 0.001);

        // average: (2s + 4s) / 2 = 3s
        Assert.assertEquals(3.0, (double) result.getValue("avg_time"), 0.001);
    }
}
