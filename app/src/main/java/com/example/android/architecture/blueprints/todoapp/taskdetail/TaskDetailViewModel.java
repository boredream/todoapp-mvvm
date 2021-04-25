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

package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.android.architecture.blueprints.todoapp.BaseViewModel;
import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;


public class TaskDetailViewModel extends BaseViewModel {

    private final MutableLiveData<Task> mTask = new MutableLiveData<>();

    private final MutableLiveData<Boolean> mIsDataAvailable = new MutableLiveData<>();

    private final MutableLiveData<Event<Object>> mEditTaskCommand = new MutableLiveData<>();

    private final MutableLiveData<Event<Object>> mDeleteTaskCommand = new MutableLiveData<>();

    private final TasksRepository mTasksRepository;

    // This LiveData depends on another so we can use a transformation.
    public final LiveData<Boolean> completed = Transformations.map(mTask,
            new Function<Task, Boolean>() {
                @Override
                public Boolean apply(Task input) {
                    return input.isCompleted();

                }
            });

    public TaskDetailViewModel(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    public void deleteTask() {
        if (mTask.getValue() != null) {
            mTasksRepository.deleteTask(mTask.getValue().getId());
            mDeleteTaskCommand.setValue(new Event<>(new Object()));
        }
    }

    public void editTask() {
        mEditTaskCommand.setValue(new Event<>(new Object()));
    }

    public MutableLiveData<Event<Object>> getEditTaskCommand() {
        return mEditTaskCommand;
    }

    public MutableLiveData<Event<Object>> getDeleteTaskCommand() {
        return mDeleteTaskCommand;
    }

    public LiveData<Task> getTask() {
        return mTask;
    }

    public LiveData<Boolean> isDataAvailable() {
        return mIsDataAvailable;
    }

    public LiveData<Boolean> getDataLoading() {
        return mDataLoading;
    }

    public void setCompleted(boolean completed) {
        if (mDataLoading.getValue()) {
            return;
        }
        Task task = this.mTask.getValue();
        if (completed) {
            mTasksRepository.completeTask(task);
            mToastSubject.onNext(R.string.task_marked_complete);
        } else {
            mTasksRepository.activateTask(task);
            mToastSubject.onNext(R.string.task_marked_active);
        }
    }

    @SuppressLint("CheckResult")
    public void start(String taskId) {
        if(taskId == null) return;

        mTasksRepository.getTask(taskId)
                .subscribe(new SingleObserver<Task>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDataLoading.setValue(true);
                    }

                    @Override
                    public void onSuccess(Task task) {
                        mTask.setValue(task);
                        mIsDataAvailable.setValue(task != null);
                        mDataLoading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mTask.setValue(null);
                        mDataLoading.setValue(false);
                        mIsDataAvailable.setValue(false);
                    }
                });
    }

    @Nullable
    protected String getTaskId() {
        return mTask.getValue().getId();
    }
}
