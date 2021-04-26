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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.example.android.architecture.blueprints.todoapp.BaseViewModel;
import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

/**
 * ViewModel for the Add/Edit screen.
 * <p>
 * This ViewModel only exposes {@link ObservableField}s, so it doesn't need to extend
 * {@link androidx.databinding.BaseObservable} and updates are notified automatically.
 */
public class AddEditTaskViewModel extends BaseViewModel {

    // Two-way databinding, exposing MutableLiveData
    public final MutableLiveData<String> title = new MutableLiveData<>();

    // Two-way databinding, exposing MutableLiveData
    public final MutableLiveData<String> description = new MutableLiveData<>();

    private final SingleLiveEvent<Boolean> mTaskUpdatedEvent = new SingleLiveEvent<>();

    private final TasksRepository mTasksRepository;

    @Nullable
    private String mTaskId;

    private boolean mIsNewTask;

    private boolean mTaskCompleted = false;

    public SingleLiveEvent<Boolean> getTaskUpdatedEvent() {
        return mTaskUpdatedEvent;
    }

    public AddEditTaskViewModel(TasksRepository tasksRepository) {
        mTasksRepository = tasksRepository;
    }

    public void start(String taskId) {
        if (mDataLoading.getValue() != null && mDataLoading.getValue()) {
            // Already loading, ignore.
            return;
        }
        mTaskId = taskId;
        if (taskId == null) {
            // No need to populate, it's a new task
            mIsNewTask = true;
            return;
        }

        mIsNewTask = false;

        mTasksRepository.getTask(taskId)
                .subscribe(new SingleObserver<Task>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDataLoading.setValue(true);
                    }

                    @Override
                    public void onSuccess(Task task) {
                        title.setValue(task.getTitle());
                        description.setValue(task.getDescription());
                        mTaskCompleted = task.isCompleted();
                        mDataLoading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mDataLoading.setValue(false);
                    }
                });
    }

    // Called when clicking on fab.
    void saveTask() {
        Task task;

        if (mIsNewTask || mTaskId == null) {
            task = new Task(title.getValue(), description.getValue());
        } else {
            task = new Task(title.getValue(), description.getValue(), mTaskId, mTaskCompleted);
        }

        if (task.isEmpty()) {
            mToastEvent.setValue("TO DOs cannot be empty");
            return;
        }

        mTasksRepository.saveTask(task)
                .subscribe(new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDataLoading.setValue(true);
                    }

                    @Override
                    public void onSuccess(String s) {
                        mTaskUpdatedEvent.setValue(mIsNewTask);
                        mDataLoading.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mDataLoading.setValue(false);
                        String action = mIsNewTask ? "add" : "update";
                        mToastEvent.setValue(action + " error = " + e.getMessage());
                    }
                });
    }

}
