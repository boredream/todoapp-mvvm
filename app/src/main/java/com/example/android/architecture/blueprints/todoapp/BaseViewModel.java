/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.SingleTransformer;


public class BaseViewModel extends ViewModel {

    // live data 可与视图直接绑定，也可以作为事件的回调
    protected final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();

    // 事件
    protected final SingleLiveEvent<String> mToastEvent = new SingleLiveEvent<>();

    public LiveData<Boolean> isDataLoading() {
        return mDataLoading;
    }

    public SingleLiveEvent<String> getToastEvent() {
        return mToastEvent;
    }

    protected <T> SingleTransformer<T, T> composeCommon() {
        return upstream -> upstream.compose(composeErrorToast())
                .compose(composeDataLoading());
    }

    protected <T> SingleTransformer<T, T> composeErrorToast() {
        return upstream -> upstream.doOnError(throwable -> mToastEvent.setValue("error = " + throwable.getMessage()));
    }

    protected <T> SingleTransformer<T, T> composeDataLoading() {
        return upstream -> upstream.doOnSubscribe(disposable -> mDataLoading.setValue(true))
                .doOnSuccess(disposable -> mDataLoading.setValue(false))
                .doOnError(throwable -> mDataLoading.setValue(false));
    }
}
