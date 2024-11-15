package org.poo.utils;

import lombok.Getter;

@Getter
public class Quintet<T1, T2, T3, T4, T5> {
    private final T1 obj0;
    private final T2 obj1;
    private final T3 obj2;
    private final T4 obj3;
    private final T5 obj4;

    public Quintet(T1 obj0, T2 obj1, T3 obj2, T4 obj3, T5 obj4) {
        this.obj0 = obj0;
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.obj3 = obj3;
        this.obj4 = obj4;
    }
}
