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

package com.example.android.architecture.blueprints.todoapp.tasks;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.android.architecture.blueprints.todoapp.BaseViewModel;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.SimpleSingleObserver;
import com.example.android.architecture.blueprints.todoapp.SingleLiveEvent;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Exposes the data to be used in the task list screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class TasksViewModel extends BaseViewModel {

    private final MutableLiveData<List<Task>> mItems = new MutableLiveData<>();

    private final MutableLiveData<Integer> mCurrentFilteringLabel = new MutableLiveData<>();

    private final MutableLiveData<Integer> mNoTasksLabel = new MutableLiveData<>();

    private final MutableLiveData<Integer> mNoTaskIconRes = new MutableLiveData<>();

    private final MutableLiveData<Boolean> mTasksAddViewVisible = new MutableLiveData<>();

    private TasksFilterType mCurrentFiltering = TasksFilterType.ALL_TASKS;

    private final TasksRepository mTasksRepository;

    // Not used at the moment
    private final SingleLiveEvent<String> mOpenTaskEvent = new SingleLiveEvent<>();

    private final SingleLiveEvent<Object> mNewTaskEvent = new SingleLiveEvent<>();

    // This LiveData depends on another so we can use a transformation.
    public final LiveData<Boolean> empty = Transformations.map(mItems, List::isEmpty);

    public TasksViewModel(TasksRepository repository) {
        mTasksRepository = repository;

        // Set initial state
        setFiltering(TasksFilterType.ALL_TASKS);
    }

    public void start() {
        loadTasks(false);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TasksFilterType#ALL_TASKS},
     *                    {@link TasksFilterType#COMPLETED_TASKS}, or
     *                    {@link TasksFilterType#ACTIVE_TASKS}
     */
    public void setFiltering(TasksFilterType requestType) {
        mCurrentFiltering = requestType;

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        switch (requestType) {
            case ALL_TASKS:
                mCurrentFilteringLabel.setValue(R.string.label_all);
                mNoTasksLabel.setValue(R.string.no_tasks_all);
                mNoTaskIconRes.setValue(R.drawable.ic_assignment_turned_in_24dp);
                mTasksAddViewVisible.setValue(true);
                break;
            case ACTIVE_TASKS:
                mCurrentFilteringLabel.setValue(R.string.label_active);
                mNoTasksLabel.setValue(R.string.no_tasks_active);
                mNoTaskIconRes.setValue(R.drawable.ic_check_circle_24dp);
                mTasksAddViewVisible.setValue(false);
                break;
            case COMPLETED_TASKS:
                mCurrentFilteringLabel.setValue(R.string.label_completed);
                mNoTasksLabel.setValue(R.string.no_tasks_completed);
                mNoTaskIconRes.setValue(R.drawable.ic_verified_user_24dp);
                mTasksAddViewVisible.setValue(false);
                break;
        }
    }

    public void clearCompletedTasks() {
        mTasksRepository.clearCompletedTasks()
                .compose(composeCommon())
                .subscribe((SimpleSingleObserver<String>) response -> {
                    mToastEvent.setValue("Completed tasks cleared");
                    loadTasks(false);
                });
    }

    public void completeTask(Task task, boolean completed) {
        // Notify repository
        if (completed) {
            mTasksRepository.completeTask(task).compose(composeCommon())
                    .subscribe((SimpleSingleObserver<String>) response -> mToastEvent.setValue("Task marked complete"));
        } else {
            mTasksRepository.activateTask(task).compose(composeCommon())
                    .subscribe((SimpleSingleObserver<String>) response -> mToastEvent.setValue("Task marked active"));
        }
    }

    // LiveData getters

    public LiveData<Boolean> getTasksAddViewVisible() {
        return mTasksAddViewVisible;
    }

    public MutableLiveData<Integer> getCurrentFilteringLabel() {
        return mCurrentFilteringLabel;
    }

    public MutableLiveData<Integer> getNoTasksLabel() {
        return mNoTasksLabel;
    }

    public MutableLiveData<Integer> getNoTaskIconRes() {
        return mNoTaskIconRes;
    }

    public LiveData<List<Task>> getItems() {
        return mItems;
    }

    public SingleLiveEvent<String> getOpenTaskEvent() {
        return mOpenTaskEvent;
    }

    public SingleLiveEvent<Object> getNewTaskEvent() {
        return mNewTaskEvent;
    }

    public void addNewTask() {
        mNewTaskEvent.setValue(new Object());
    }

    void openTask(String taskId) {
        mOpenTaskEvent.setValue(taskId);
    }

    void handleActivityResult(int requestCode, int resultCode) {
        if (AddEditTaskActivity.REQUEST_CODE == requestCode) {
            switch (resultCode) {
                case TaskDetailActivity.EDIT_RESULT_OK:
                    mToastEvent.setValue("TO-DO saved");
                    loadTasks(false);
                    break;
                case AddEditTaskActivity.ADD_EDIT_RESULT_OK:
                    mToastEvent.setValue("TO-DO added");
                    loadTasks(true);
                    break;
                case TaskDetailActivity.DELETE_RESULT_OK:
                    mToastEvent.setValue("Task was deleted");
                    loadTasks(false);
                    break;
            }
        }
    }

    public void loadTasks(boolean forceUpdate) {
        if (forceUpdate) {
            mTasksRepository.setCacheIsDirty(true);
        }

        mTasksRepository.getTasks()
                .map(this::filterList)
                .compose(composeCommon())
                .subscribe((SimpleSingleObserver<List<Task>>) mItems::setValue);
    }

    private List<Task> filterList(List<Task> tasks) {
        if (tasks == null) return null;

        List<Task> tasksToShow = new ArrayList<>();

        // We filter the tasks based on the requestType
        for (Task task : tasks) {
            switch (mCurrentFiltering) {
                case ACTIVE_TASKS:
                    if (task.isActive()) {
                        tasksToShow.add(task);
                    }
                    break;
                case COMPLETED_TASKS:
                    if (task.isCompleted()) {
                        tasksToShow.add(task);
                    }
                    break;
                case ALL_TASKS:
                default:
                    tasksToShow.add(task);
                    break;
            }
        }
        return tasksToShow;
    }

}
