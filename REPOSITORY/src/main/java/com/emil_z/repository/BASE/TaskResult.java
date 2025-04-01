package com.emil_z.repository.BASE;

public class TaskResult<T> {
    private final T result;

    public TaskResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}