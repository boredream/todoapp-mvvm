package com.example.android.architecture.blueprints.todoapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.Disposable;

abstract public class BaseActivity extends AppCompatActivity {

    abstract protected BaseViewModel genViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup(genViewModel());
    }

    private void setup(BaseViewModel viewModel) {
        setupToast(viewModel);
    }

    private void setupToast(BaseViewModel viewModel) {
        Disposable disposable = viewModel.getToastSubject().subscribe(msgId ->
                Toast.makeText(this, getString(msgId), Toast.LENGTH_SHORT).show());
    }
}
