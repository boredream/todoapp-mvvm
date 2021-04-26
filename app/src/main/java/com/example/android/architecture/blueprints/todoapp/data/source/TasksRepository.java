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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 实际开发中简化只用 remote + cache，去掉 local db（远程这里用room + 延迟模拟接口获取）
 */
public class TasksRepository {

    private static final int SERVICE_LATENCY_IN_MILLIS = 1000;
    private volatile static TasksRepository INSTANCE = null;
    private TasksDao mTasksDao;

    // 只在get/delete/update时使用内存。其它情况如add之后会重新拉取，因此这类操作不处理内存。根据需求可自行决定。
    private List<Task> mCachedTasks;
    private boolean mCacheIsDirty;

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

    public void setCacheIsDirty(boolean cacheIsDirty) {
        mCacheIsDirty = cacheIsDirty;
    }

    private <T> SingleTransformer<T, T> getSingleTransformer() {
        return upstream -> upstream.delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Task>> getTasks() {
        if (mCachedTasks != null && !mCacheIsDirty) {
            return Single.just(mCachedTasks);
        }

        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<List<Task>>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            refreshCache(mTasksDao.getTasks());
            emitter.onSuccess(mCachedTasks);
        }).compose(getSingleTransformer());
    }

    public Single<String> saveTask(@NonNull final Task task) {
        // 新增后服务器一般会setId等操作，所以数据直接加入缓存有问题，应该重新拉取数据
        // 不过，如果新增接口的response是服务器处理后的数据，则可以加入到缓存里，无需再次请求

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
            if (mCachedTasks != null) {
                for (Task cacheTask : mCachedTasks) {
                    if (task.getId().equals(cacheTask.getId())) {
                        cacheTask.setCompleted(true);
                        break;
                    }
                }
            }
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> completeTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(taskId, true);
            if (mCachedTasks != null) {
                for (Task cacheTask : mCachedTasks) {
                    if (taskId.equals(cacheTask.getId())) {
                        cacheTask.setCompleted(true);
                        break;
                    }
                }
            }
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> activateTask(@NonNull final Task task) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(task.getId(), false);
            if (mCachedTasks != null) {
                for (Task cacheTask : mCachedTasks) {
                    if (task.getId().equals(cacheTask.getId())) {
                        cacheTask.setCompleted(false);
                        break;
                    }
                }
            }
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> activateTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.updateCompleted(taskId, false);
            if (mCachedTasks != null) {
                for (Task cacheTask : mCachedTasks) {
                    if (taskId.equals(cacheTask.getId())) {
                        cacheTask.setCompleted(true);
                        break;
                    }
                }
            }
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> clearCompletedTasks() {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.deleteCompletedTasks();
            if (mCachedTasks != null) {
                Iterator<Task> iterator = mCachedTasks.iterator();
                for (; iterator.hasNext(); ) {
                    Task next = iterator.next();
                    if (next.isCompleted()) {
                        iterator.remove();
                    }
                }
            }
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<Task> getTask(@NonNull final String taskId) {
        if (mCachedTasks != null && !mCacheIsDirty) {
            for (Task task : mCachedTasks) {
                if (taskId.equals(task.getId())) {
                    return Single.just(task);
                }
            }
        }

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
            refreshCache(new ArrayList<>());
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    public Single<String> deleteTask(@NonNull final String taskId) {
        EspressoIdlingResource.increment(); // App is busy until further notice
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            EspressoIdlingResource.decrement(); // Set app as idle.
            mTasksDao.deleteTaskById(taskId);
            if (mCachedTasks != null) {
                Iterator<Task> iterator = mCachedTasks.iterator();
                for (; iterator.hasNext(); ) {
                    Task next = iterator.next();
                    if (taskId.equals(next.getId())) {
                        iterator.remove();
                        break;
                    }
                }
            }
            emitter.onSuccess("ok");
        }).compose(getSingleTransformer());
    }

    private void refreshCache(List<Task> tasks) {
        if (mCachedTasks == null) {
            mCachedTasks = new ArrayList<>();
        }
        mCachedTasks.clear();
        if (tasks != null) mCachedTasks.addAll(tasks);
        mCacheIsDirty = false;
    }
}
