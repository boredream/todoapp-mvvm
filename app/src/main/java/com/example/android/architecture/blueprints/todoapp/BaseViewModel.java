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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;


public class BaseViewModel extends ViewModel {

    // live data 直接绑定
    protected final MutableLiveData<Boolean> mDataLoading = new MutableLiveData<>();

    // 事件用 subject，获取监听回调
    protected final PublishSubject<Integer> mToastSubject;

    public BaseViewModel() {
        mToastSubject = PublishSubject.create();
    }

    public LiveData<Boolean> isDataLoading() {
        return mDataLoading;
    }

    @NonNull
    public Observable<Integer> getToastSubject() {
        return mToastSubject;
    }
}
