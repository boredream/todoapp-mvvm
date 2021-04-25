/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.android.architecture.blueprints.todoapp.BaseActivity;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.databinding.TaskdetailActBinding;

import static com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity.ADD_EDIT_RESULT_OK;

/**
 * Displays task details screen.
 */
public class TaskDetailActivity extends BaseActivity<TaskDetailViewModel, TaskdetailActBinding> {

    public static final String EXTRA_TASK_ID = "TASK_ID";
    public static final int REQUEST_EDIT_TASK = 1;

    public static final int DELETE_RESULT_OK = RESULT_FIRST_USER + 2;

    public static final int EDIT_RESULT_OK = RESULT_FIRST_USER + 3;

    private String taskId;

    @Override
    protected int getLayoutId() {
        return R.layout.taskdetail_act;
    }

    @Override
    protected Class<TaskDetailViewModel> getViewModelClass() {
        return TaskDetailViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupProgressDialog();
        setupToolbar();
        setupTaskDetailUserActionsListener();
        subscribeToNavigationChanges();

        taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        mViewModel.start(taskId);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    private void setupTaskDetailUserActionsListener() {
        mBinding.setListener(v -> mViewModel.setCompleted(((CheckBox) v).isChecked()));
    }

    private void subscribeToNavigationChanges() {
        // The activity observes the navigation commands in the ViewModel
        mViewModel.getEditTaskCommand().observe(this, taskEvent -> {
            if (taskEvent.getContentIfNotHandled() != null) {
                TaskDetailActivity.this.onStartEditTask(taskId);
            }
        });
        mViewModel.getDeleteTaskCommand().observe(this, taskEvent -> {
            if (taskEvent.getContentIfNotHandled() != null) {
                TaskDetailActivity.this.onTaskDeleted();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mViewModel.deleteTask();
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.taskdetail_fragment_menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == ADD_EDIT_RESULT_OK) {
                // If the result comes from the add/edit screen, it's an edit.
                setResult(EDIT_RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onTaskDeleted() {
        setResult(DELETE_RESULT_OK);
        // If the task was deleted successfully, go back to the list.
        finish();
    }

    public void onStartEditTask(String taskId) {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

}
