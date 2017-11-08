package org.skife.muckery;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Tristan {
    public static void main(final String[] args) {
        try {
            final URL url = new URL("http://interview.with.tristan.blea.se/?limit=100&perChunk=100&seed=1");
            final Map<String, Integer> rs = new HashMap<>();
            try (InputStream in = new BufferedInputStream(url.openStream())) {
                Scanner scan = new Scanner(in);
                while (scan.hasNext()) {
                    String word = scan.next();
                    rs.compute(word, (_k, c) -> c == null ? 1 : ++c);
                }
            }
            rs.entrySet()
              .stream()
              .sorted((a, b) -> b.getValue() - a.getValue())
              .limit(20)
              .forEach((e) -> System.out.printf("%s\t%d%n", e.getKey(), e.getValue()));
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
