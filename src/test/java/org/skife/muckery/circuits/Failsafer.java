package org.skife.muckery.circuits;

import net.jodah.failsafe.function.CheckedConsumer;

public class Failsafer {
    public static <T extends Throwable> CheckedConsumer<T> bounce() {
        return t -> {
            if (t instanceof Exception) {
                throw (Exception) t;
            }
            else {
                throw new Exception(t.getMessage(), t);
            }
        };
    }
}
