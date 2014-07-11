package org.skife.muckery.mappy;

import java.util.Arrays;

public class Utils
{
    /**
     * Determines if two objects are equal as determined by
     * {@link Object#equals(Object)}, or "deeply equal" if both are arrays.
     * <p>
     * If both objects are null, true is returned; if both objects are array,
     * the corresponding {@link Arrays#deepEquals(Object[], Object[])}, or
     * {@link Arrays#equals(int[], int[])} or the like are called to determine
     * equality.
     * <p>
     * Note that this method does not "deeply" compare the fields of the
     * objects.
     */
    public static boolean deepEquals(Object o1, Object o2) {
        if(o1 == o2) {
            return true;
        }
        if(o1 == null || o2 == null) {
            return false;
        }

        Class<?> type1 = o1.getClass();
        Class<?> type2 = o2.getClass();
        if(!(type1.isArray() && type2.isArray())) {
            return o1.equals(o2);
        }
        if(o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.deepEquals((Object[]) o1, (Object[]) o2);
        }
        if(type1 != type2) {
            return false;
        }
        if(o1 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
        if(o1 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if(o1 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        }
        if(o1 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        }
        if(o1 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        }
        if(o1 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        }
        if(o1 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        }
        if(o1 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        }
        throw new AssertionError();
    }
}
