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

package com.example.android.architecture.blueprints.todoapp.data.source;

import androidx.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 实际开发中简化只用远程数据（用db+延迟模拟接口获取）
 */
public class TasksRepository {

    private static final int SERVICE_LATENCY_IN_MILLIS = 1000;
    private TasksDao mTasksDao;

    private volatile static TasksRepository INSTANCE = null;

    // Prevent direct instantiation.
    private TasksRepository(TasksDao tasksDao) {
        mTasksDao = tasksDao;
    }

    public static TasksRepository getInstance(TasksDao tasksDao) {
        if (INSTANCE == null) {
            synchronized (TasksRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TasksRepository(tasksDao);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private <T> SingleTransformer<T, T> getSingleTransformer() {
        return upstream -> upstream.delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Task>> getTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<List<Task>>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            emitter.onSuccess(mTasksDao.getTasks());
        }).compose(getSingleTransformer());
    }

    public Single<String> saveTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.insertTask(task);
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> completeTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(task.getId(), true);
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> completeTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(taskId, true);
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> activateTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(task.getId(), false);
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> activateTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(taskId, false);
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> clearCompletedTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.deleteCompletedTasks();
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<Task> getTask(@NonNull final String taskId) {
        System.out.println("getTask " + taskId);
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<Task>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            emitter.onSuccess(mTasksDao.getTaskById(taskId));
        }).compose(getSingleTransformer());
    }

    public Single<String> deleteAllTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.deleteTasks();
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> deleteTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.deleteTaskById(taskId);
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }
}
