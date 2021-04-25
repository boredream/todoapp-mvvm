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

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.example.android.architecture.blueprints.todoapp.BaseActivity;
import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.databinding.AddtaskActBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays an add or edit task screen.
 */
public class AddEditTaskActivity extends BaseActivity<AddEditTaskViewModel, AddtaskActBinding> {

    public static final int REQUEST_CODE = 1;

    public static final int ADD_EDIT_RESULT_OK = RESULT_FIRST_USER + 1;
    public static final String ARGUMENT_EDIT_TASK_ID = "taskId";

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.addtask_act;
    }

    @Override
    protected Class<AddEditTaskViewModel> getViewModelClass() {
        return AddEditTaskViewModel.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar();
        setupFab();
        setupActionBar();

        loadData();

        subscribeToNavigationChanges();
    }

    private void setupToolbar() {
        // Set up the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_edit_task_done);
        fab.setImageResource(R.drawable.ic_done);
        fab.setOnClickListener(v -> mViewModel.saveTask());
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        if (getIntent().getStringExtra(AddEditTaskActivity.ARGUMENT_EDIT_TASK_ID) != null) {
            actionBar.setTitle(R.string.edit_task);
        } else {
            actionBar.setTitle(R.string.add_task);
        }
    }

    private void loadData() {
        // Add or edit an existing task?
        if (getIntent().getStringExtra(AddEditTaskActivity.ARGUMENT_EDIT_TASK_ID) != null) {
            mViewModel.start(getIntent().getStringExtra(AddEditTaskActivity.ARGUMENT_EDIT_TASK_ID));
        } else {
            mViewModel.start(null);
        }
    }

    private void subscribeToNavigationChanges() {
        // The activity observes the navigation events in the ViewModel
        addSubject(mViewModel.getTaskUpdatedEvent().subscribe(isNewTask -> {
            setResult(ADD_EDIT_RESULT_OK);
            finish();
        }));
    }

}
