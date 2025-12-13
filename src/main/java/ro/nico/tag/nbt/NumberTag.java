package ro.nico.tag.nbt;

/**
 * A number tag.
 */
public interface NumberTag extends Tag {
    /**
     * Gets the byte value.
     *
     * @return the byte value
     */
    byte byteValue();

    /**
     * Gets the double value.
     *
     * @return the double value
     */
    double doubleValue();

    /**
     * Gets the float value.
     *
     * @return the float value
     */
    float floatValue();

    /**
     * Gets the int value.
     *
     * @return the int value
     */
    int intValue();

    /**
     * Gets the long value.
     *
     * @return the long value
     */
    long longValue();

    /**
     * Gets the short value.
     *
     * @return the short value
     */
    short shortValue();

    // rather than depending on math as a whole, we'll just copy these two methods over
    // https://github.com/KyoriPowered/math/blob/master/src/main/java/net/kyori/math/Mth.java
    static int floor(final double n) {
        final int i = (int) n;
        return n < (double) i ? i - 1 : i;
    }

    static int floor(final float n) {
        final int i = (int) n;
        return n < (float) i ? i - 1 : i;
    }
}
