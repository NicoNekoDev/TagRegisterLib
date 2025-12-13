package ro.nico.tag.util;

import java.util.Objects;

@FunctionalInterface
public interface TripletConsumer<K1, K2, K3> {
    void accept(K1 value1, K2 value2, K3 value3);

    default TripletConsumer<K1, K2, K3> andThen(TripletConsumer<? super K1, ? super K2, ? super K3> after) {
        Objects.requireNonNull(after);
        return (value1, value2, value3) -> {
            accept(value1, value2, value3);
            after.accept(value1, value2, value3);
        };
    }
}