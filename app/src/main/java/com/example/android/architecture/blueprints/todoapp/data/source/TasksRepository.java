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
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * 实际开发中简化只用远程数据（用db+延迟模拟接口获取）
 */
public class TasksRepository {

    private static final int SERVICE_LATENCY_IN_MILLIS = 2000;
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

    public Single<List<Task>> getTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<List<Task>>() {
            @Override
            public void subscribe(SingleEmitter<List<Task>> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                emitter.onSuccess(mTasksDao.getTasks());
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> saveTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.insertTask(task);
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> completeTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.updateCompleted(task.getId(), true);
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> completeTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.updateCompleted(taskId, true);
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> activateTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.updateCompleted(task.getId(), false);
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> activateTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.updateCompleted(taskId, false);
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> clearCompletedTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.deleteCompletedTasks();
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<Task> getTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<Task>() {
            @Override
            public void subscribe(SingleEmitter<Task> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                emitter.onSuccess(mTasksDao.getTaskById(taskId));
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public void refreshTasks() {
//        mCacheIsDirty = true;
    }

    public Single<String> deleteAllTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.deleteTasks();
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    public Single<String> deleteTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mTasksDao.deleteTaskById(taskId);
                emitter.onSuccess("ok");
            }
        }).delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }
}
