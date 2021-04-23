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
import androidx.lifecycle.ViewModel;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;


public class BaseViewModel extends ViewModel {

//    // using a BehaviourSubject because we are interested in the last object that was emitted before
//    // subscribing. Like this we ensure that the loading indicator has the correct visibility.
//    protected final BehaviorSubject<Boolean> mLoadingIndicatorSubject;

    // using a PublishSubject because we are not interested in the last object that was emitted
    // before subscribing. Like this we avoid displaying the snackbar multiple times
    @NonNull
    protected final PublishSubject<Integer> mSnackbarText;

    public BaseViewModel() {
//        mLoadingIndicatorSubject = BehaviorSubject.createDefault(false);
        mSnackbarText = PublishSubject.create();
    }

    @NonNull
    public Observable<Integer> getSnackbarMessage() {
        return mSnackbarText;
    }

//    /**
//     * @return a stream that emits true if the progress indicator should be displayed, false otherwise.
//     */
//    @NonNull
//    public Observable<Boolean> getLoadingIndicatorVisibility() {
//        return mLoadingIndicatorSubject;
//    }
}
