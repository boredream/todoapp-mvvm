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


import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link AddEditTaskViewModel}.
 */
public class AddEditTaskViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private TasksRepository mTasksRepository;

    private AddEditTaskViewModel mAddEditTaskViewModel;

    private TestObserver<Boolean> mTaskUpdatedTestObserver;

    @Before
    public void setupAddEditTaskViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mAddEditTaskViewModel = new AddEditTaskViewModel(mTasksRepository);
        mTaskUpdatedTestObserver = new TestObserver<>();
    }

    @Test
    public void saveNewTask() {
        when(mTasksRepository.saveTask(any(Task.class))).thenReturn(Single.just("ok"));
        mAddEditTaskViewModel.getTaskUpdatedEvent().subscribe(mTaskUpdatedTestObserver);

        mAddEditTaskViewModel.start(null);
        mAddEditTaskViewModel.description.setValue("Some Task Description");
        mAddEditTaskViewModel.title.setValue("New Task Title");
        mAddEditTaskViewModel.saveTask();

        mTaskUpdatedTestObserver.assertSubscribed();
        mTaskUpdatedTestObserver.assertValue(true);
    }

    @Test
    public void populateTaskAndUpdate() {
        when(mTasksRepository.saveTask(any(Task.class))).thenReturn(Single.just("ok"));
        Task testTask = new Task("TITLE", "DESCRIPTION", "1");

        // When the ViewModel is asked to populate an existing task
        mAddEditTaskViewModel.getTaskUpdatedEvent().subscribe(mTaskUpdatedTestObserver);
        when(mTasksRepository.getTask(testTask.getId())).thenReturn(Single.just(testTask));
        mAddEditTaskViewModel.start(testTask.getId());

        // Then the task repository is queried and the view updated
        mTasksRepository.getTask(testTask.getId()).test().assertSubscribed();

        // Verify the fields were updated
        assertThat(mAddEditTaskViewModel.title.getValue(), is(testTask.getTitle()));
        assertThat(mAddEditTaskViewModel.description.getValue(), is(testTask.getDescription()));

        // Update
        String updateStr = "Update Task Title";
        mAddEditTaskViewModel.title.setValue(updateStr);
        mAddEditTaskViewModel.saveTask();

        mTaskUpdatedTestObserver.assertSubscribed();
        mTaskUpdatedTestObserver.assertValue(false);
        assertThat(mAddEditTaskViewModel.title.getValue(), is(updateStr));
        assertThat(mAddEditTaskViewModel.description.getValue(), is(testTask.getDescription()));
    }
}
