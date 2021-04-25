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

import android.app.Application;
import android.content.res.Resources;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.android.architecture.blueprints.todoapp.Event;
import com.example.android.architecture.blueprints.todoapp.LiveDataTestUtil;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static com.example.android.architecture.blueprints.todoapp.R.string.successfully_deleted_task_message;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TasksViewModel}
 */
public class TasksViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private Application mContext;

    private TasksViewModel mTasksViewModel;

    private TestObserver<Integer> mToastTestSubscriber;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        // Get a reference to the class under test
        mTasksViewModel = new TasksViewModel(mTasksRepository);
        mToastTestSubscriber = new TestObserver<>();

        // We initialise the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.successfully_saved_task_message))
                .thenReturn("EDIT_RESULT_OK");
        when(mContext.getString(R.string.successfully_added_task_message))
                .thenReturn("ADD_EDIT_RESULT_OK");
        when(mContext.getString(successfully_deleted_task_message))
                .thenReturn("DELETE_RESULT_OK");

        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void loadAllTasksFromRepository() {
        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);

        when(mTasksRepository.getTasks()).thenReturn(Single.just(TASKS));
        mTasksViewModel.loadTasks(true);

        // And data loaded
        assertFalse(mTasksViewModel.getItems().getValue().isEmpty());
        assertEquals(3, mTasksViewModel.getItems().getValue().size());
    }

    @Test
    public void loadActiveTasksFromRepository() {
        mTasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS);

        when(mTasksRepository.getTasks()).thenReturn(Single.just(TASKS));
        mTasksViewModel.loadTasks(true);

        // And data loaded
        assertFalse(mTasksViewModel.getItems().getValue().isEmpty());
        assertEquals(1, mTasksViewModel.getItems().getValue().size());
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        mTasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS);

        when(mTasksRepository.getTasks()).thenReturn(Single.just(TASKS));
        mTasksViewModel.loadTasks(true);

        // And data loaded
        assertFalse(mTasksViewModel.getItems().getValue().isEmpty());
        assertEquals(2, mTasksViewModel.getItems().getValue().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void clickOnFab_ShowsAddTaskUi() throws InterruptedException {
        // When adding a new task
        mTasksViewModel.addNewTask();

        // Then the event is triggered
        Event<Object> value = mTasksViewModel.getNewTaskEvent().getValue();
        assertNotNull(value.getContentIfNotHandled());
    }

    @Test
    public void clearCompletedTasks_ClearsTasks() {
        when(mTasksRepository.clearCompletedTasks()).thenReturn(Single.just("ok"));
        when(mTasksRepository.getTasks()).thenReturn(Single.just(new ArrayList<>()));
        mTasksViewModel.getToastSubject().subscribe(mToastTestSubscriber);
        mTasksViewModel.clearCompletedTasks();

        // And data loaded
        mToastTestSubscriber.assertValue(R.string.completed_tasks_cleared);
        assertTrue(mTasksViewModel.getItems().getValue().isEmpty());
    }

    @Test
    public void handleActivityResult_editOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        mTasksViewModel.getToastSubject().subscribe(mToastTestSubscriber);
        mTasksViewModel.handleActivityResult(AddEditTaskActivity.REQUEST_CODE, TaskDetailActivity.EDIT_RESULT_OK);
        mToastTestSubscriber.assertValue(R.string.successfully_saved_task_message);
    }

    @Test
    public void handleActivityResult_addEditOK() {
        // When TaskDetailActivity sends a EDIT_RESULT_OK
        mTasksViewModel.getToastSubject().subscribe(mToastTestSubscriber);
        mTasksViewModel.handleActivityResult(AddEditTaskActivity.REQUEST_CODE, AddEditTaskActivity.ADD_EDIT_RESULT_OK);
        mToastTestSubscriber.assertValue(R.string.successfully_added_task_message);
    }

    @Test
    public void handleActivityResult_deleteOk() {
        // When TaskDetailActivity sends a DELETE_RESULT_OK
        mTasksViewModel.getToastSubject().subscribe(mToastTestSubscriber);
        mTasksViewModel.handleActivityResult(AddEditTaskActivity.REQUEST_CODE, TaskDetailActivity.DELETE_RESULT_OK);
        mToastTestSubscriber.assertValue(R.string.successfully_deleted_task_message);
    }

    @Test
    public void getTasksAddViewVisible() throws InterruptedException {
        // When the filter type is ALL_TASKS
        mTasksViewModel.setFiltering(TasksFilterType.ALL_TASKS);

        // Then the "Add task" action is visible
        assertTrue(LiveDataTestUtil.getValue(mTasksViewModel.getTasksAddViewVisible()));
    }
}
