package org.skife.muckery.flogger;

import com.google.common.collect.Lists;
import com.google.common.flogger.FluentLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class FloggerTest {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private List<Handler> handlers;
    private List<LogRecord> records;

    @Before
    public void setUp() throws Exception {
        LogManager lm = LogManager.getLogManager();
        lm.reset();

        records = Lists.newArrayList();

        Logger jul = Logger.getLogger(FloggerTest.class.getName());
        handlers = Arrays.asList(jul.getHandlers());
        handlers.forEach(jul::removeHandler);
        jul.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }

    @After
    public void tearDown() throws Exception {
        Logger jul = Logger.getLogger(FloggerTest.class.getName());
        handlers.forEach(jul::addHandler);
    }

    @Test
    public void testFoo() throws Exception {
        logger.atInfo().log("hello world");
        logger.atInfo().log("hello world");

        assertThat(records).hasSize(2);
    }
}
