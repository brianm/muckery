package org.skife.muckery.opencensus;

import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import org.junit.Test;

import java.util.List;

public class TestStats {
    @Test
    public void testFoo() throws Exception {
        Tagger tagger = Tags.getTagger();
        ViewManager viewManager = Stats.getViewManager();
        StatsRecorder statsRecorder = Stats.getStatsRecorder();



        Measure.MeasureLong ml = Measure.MeasureLong.create("size", "size of the thing", "units");
        statsRecorder.newMeasureMap().put(ml, 1);


    }
}
