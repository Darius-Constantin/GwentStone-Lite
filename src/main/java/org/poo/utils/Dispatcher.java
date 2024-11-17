package org.poo.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.function.Consumer;

@Getter
public final class Dispatcher<T1> {
    private final ArrayList<Consumer<T1>> subscribers = new ArrayList<>();

    public void register(final Consumer<T1> handler) {
        subscribers.add(handler);
    }

    public void dispatch(final T1 event) {
        for (Consumer<T1> function : subscribers) {
            function.accept(event);
        }
    }
}
