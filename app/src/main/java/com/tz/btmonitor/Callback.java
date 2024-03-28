package com.tz.btmonitor;

public interface Callback<T> {
    void call(T t);
}