package com.example.android.architecture.blueprints.todoapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import io.reactivex.disposables.Disposable;

abstract public class BaseActivity<VM extends BaseViewModel, BD extends ViewDataBinding> extends AppCompatActivity {

    protected VM mViewModel;
    protected BD mBinding;

    abstract protected int getLayoutId();

    abstract protected Class<VM> genViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, getLayoutId());
        ViewModelFactory factory = ViewModelFactory.getInstance(getApplication());
        mViewModel = new ViewModelProvider(this, factory).get(genViewModel());

        mBinding.setLifecycleOwner(this);
        mBinding.setVariable(BR.viewModel, mViewModel);

        setupToast();
    }

    private void setupToast() {
        Disposable disposable = mViewModel.getToastSubject().subscribe(msgId ->
                Toast.makeText(this, getString(msgId), Toast.LENGTH_SHORT).show());
    }
}
