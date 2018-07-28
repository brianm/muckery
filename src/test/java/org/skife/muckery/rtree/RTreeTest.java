package org.skife.muckery.rtree;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.internal.EntryDefault;
import com.google.common.io.Resources;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class RTreeTest {

    private static final RTree<String, Geometry> airports = loadAirports();
    private static final Entry<String, Geometry> NONE = EntryDefault.entry("NON", Geometries.pointGeographic(0, 0));

    @Test
    public void testFoo() throws Exception {

        final Point marriotWhitefield = Geometries.pointGeographic(12.9796, 77.7277);
        final Point seattleOffice = Geometries.pointGeographic(47.607148, -122.3381338);

        airports.nearest(marriotWhitefield, 200 /* km */, 1).defaultIfEmpty(NONE).subscribe(entry -> {
            assertThat(entry.value()).isEqualTo("BLR");
        });

        airports.nearest(seattleOffice, 200 /* km */, 1).defaultIfEmpty(NONE).subscribe(entry -> {
            assertThat(entry.value()).isEqualTo("BFI");
        });

    }

    private static RTree<String, Geometry> loadAirports() {
        RTree<String, Geometry> tree = RTree.create();

        // load and parse airports.dat to populate airports into our rtree
        // data is sourced initially from https://openflights.org/data.html
        try (InputStream in = Resources.getResource("airports.dat").openStream()) {
            CsvParserSettings settings = new CsvParserSettings();
            settings.detectFormatAutomatically(',');
            for (String[] row : new CsvParser(settings).parseAll(in, StandardCharsets.UTF_8)) {
                String iata = row[4];
                if (iata == null || iata.length() != 3) {
                    // no iata, is a teensy weird little airport, so skip
                    continue;
                }
                tree = tree.add(iata, Geometries.pointGeographic(Float.parseFloat(row[6]), Float.parseFloat(row[7])));
            }
        } catch (IOException e) {
            throw new IllegalStateException("unable to load airports.dat resource", e);
        }

        return tree;
    }
}
