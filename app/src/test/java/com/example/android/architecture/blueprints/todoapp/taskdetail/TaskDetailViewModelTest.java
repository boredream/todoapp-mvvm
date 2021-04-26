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


import android.app.Application;
import android.content.res.Resources;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Single;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link TaskDetailViewModel}
 */
public class TaskDetailViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private static final String TITLE_TEST = "title";

    private static final String DESCRIPTION_TEST = "description";

    private static final String NO_DATA_STRING = "NO_DATA_STRING";

    private static final String NO_DATA_DESC_STRING = "NO_DATA_DESC_STRING";

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private Application mContext;

    private TaskDetailViewModel mTaskDetailViewModel;

    private Task mTask;

    @Before
    public void setupTasksViewModel() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        setupContext();

        mTask = new Task();
        mTask.setId("1");
        mTask.setTitle(TITLE_TEST);
        mTask.setDescription(DESCRIPTION_TEST);

        // Get a reference to the class under test
        mTaskDetailViewModel = new TaskDetailViewModel(mTasksRepository);
    }

    private void setupContext() {
        when(mContext.getApplicationContext()).thenReturn(mContext);
        when(mContext.getString(R.string.no_data)).thenReturn(NO_DATA_STRING);
        when(mContext.getString(R.string.no_data_description)).thenReturn(NO_DATA_DESC_STRING);
        when(mContext.getResources()).thenReturn(mock(Resources.class));
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        setupViewModelRepository();

        // Then verify that the view was notified
        assertEquals(mTaskDetailViewModel.getTask().getValue().getTitle(), mTask.getTitle());
        assertEquals(mTaskDetailViewModel.getTask().getValue().getDescription(), mTask.getDescription());
    }

    @Test
    public void deleteTask() {
        setupViewModelRepository();
        when(mTasksRepository.deleteTask(mTask.getId())).thenReturn(Single.just("ok"));

        // When the deletion of a task is requested
        mTaskDetailViewModel.deleteTask();

        // Then the repository is notified
        assertNotNull(mTaskDetailViewModel.getDeleteTaskCommand().getValue());
    }

    @Test
    public void completeTask() {
        setupViewModelRepository();
        when(mTasksRepository.completeTask(mTask)).thenReturn(Single.just("ok"));

        // When the ViewModel is asked to complete the task
        mTaskDetailViewModel.setCompleted(true);

        // Then a request is sent to the task repository and the UI is updated
        assertEquals("Task marked complete", mTaskDetailViewModel.getToastEvent().getValue());
    }

    @Test
    public void activateTask() {
        setupViewModelRepository();
        when(mTasksRepository.activateTask(mTask)).thenReturn(Single.just("ok"));

        // When the ViewModel is asked to complete the task
        mTaskDetailViewModel.setCompleted(false);

        // Then a request is sent to the task repository and the UI is updated
        assertEquals("Task marked active", mTaskDetailViewModel.getToastEvent().getValue());
    }

    @Test
    public void TaskDetailViewModel_repositoryError() {
        when(mTasksRepository.getTask(mTask.getId())).thenReturn(Single.error(new RuntimeException()));

        // Given an initialized ViewModel with an active task
        mTaskDetailViewModel.start(mTask.getId());

        // Then verify that data is not available
        assertNotNull(mTaskDetailViewModel.getToastEvent().getValue());
        assertFalse(mTaskDetailViewModel.isDataAvailable().getValue());
    }

//    @Test
//    public void TaskDetailViewModel_repositoryNull() {
//        setupViewModelRepository();
//
//        // When the repository returns a null task
//        mGetTaskCallbackCaptor.getValue().onTaskLoaded(null); // Trigger callback error
//
//        // Then verify that data is not available
//        assertFalse(LiveDataTestUtil.getValue(mTaskDetailViewModel.getIsDataAvailable()));
//
//        // Then task detail UI is shown
//        assertThat(mTaskDetailViewModel.getTask().getValue(), is(nullValue()));
//    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        mTaskDetailViewModel.editTask();

        // Then the event is triggered
        assertNotNull(mTaskDetailViewModel.getEditTaskCommand().getValue());
    }

    private void setupViewModelRepository() {
        when(mTasksRepository.getTask(mTask.getId())).thenReturn(Single.just(mTask));

        // Given an initialized ViewModel with an active task
        mTaskDetailViewModel.start(mTask.getId());

        // Use a captor to get a reference for the callback.
        mTasksRepository.getTask(mTask.getId()).test().assertSubscribed();
    }
}
