package com.example.android.architecture.blueprints.todoapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

abstract public class BaseActivity<VM extends BaseViewModel, BD extends ViewDataBinding> extends AppCompatActivity {

    protected VM mViewModel;
    protected BD mBinding;

    abstract protected int getLayoutId();

    abstract protected Class<VM> getViewModelClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, getLayoutId());
        ViewModelFactory factory = ViewModelFactory.getInstance(getApplication());
        mViewModel = new ViewModelProvider(this, factory).get(getViewModelClass());
        mBinding.setLifecycleOwner(this);
        mBinding.setVariable(BR.viewModel, mViewModel);

        setupToast();
    }

    @Deprecated
    private ProgressDialog progressDialog;

    @Deprecated
    protected void setupProgressDialog() {
        // 建议用progress bar嵌入式加到布局中
        progressDialog = new ProgressDialog(this);
        mViewModel.isDataLoading().observe(this, show -> {
            System.out.println("show loading = " + show);
            if (show && !progressDialog.isShowing()) progressDialog.show();
            else if (!show && progressDialog.isShowing()) progressDialog.dismiss();
        });
    }

    private void setupToast() {
        mViewModel.getToastEvent().observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

}
