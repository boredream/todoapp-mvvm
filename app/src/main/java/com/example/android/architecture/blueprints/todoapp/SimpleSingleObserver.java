package com.example.android.architecture.blueprints.todoapp;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public interface SimpleSingleObserver<T> extends SingleObserver<T> {

    @Override
    void onSuccess(T response);

    @Override
    default void onSubscribe(Disposable d) {

    }

    @Override
    default void onError(Throwable e) {

    }
}
