package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.helper.inputValidators.CompareRule;
import com.emil_z.helper.inputValidators.NameRule;
import com.emil_z.helper.inputValidators.Rule;
import com.emil_z.helper.inputValidators.RuleOperation;
import com.emil_z.helper.inputValidators.Validator;
import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class RegisterActivity extends BaseActivity {
	private EditText etUsername;
	private EditText etPassword;
	private EditText etConfirmPassword;
	private Button btnRegister;
	private Button btnBack;

	private UsersViewModel viewModel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_register);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setListeners();
		setViewModel();

	}
	protected void initializeViews() {
		etUsername = findViewById(R.id.etUsername);
		etPassword = findViewById(R.id.etPassword);
		etConfirmPassword = findViewById(R.id.etConfirmPassword);
		btnRegister = findViewById(R.id.btnRegister);
		btnBack = findViewById(R.id.btnBack);
	}

	protected void setListeners() {
		btnRegister.setOnClickListener(v -> {
			if(validate()) {
				viewModel.exist(etUsername.getText().toString());
			}
		});
		btnBack.setOnClickListener(v -> {
			finish();
		});

	}

	protected void registerUser() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar_default);
		User user = new User(etUsername.getText().toString(),
				etPassword.getText().toString(),
				BitMapHelper.encodeTobase64(bitmap));
		viewModel.save(user);
		Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataSuccess().observe(this, new Observer<Boolean>() {
			public void onChanged(Boolean aBoolean) {
				Toast.makeText(RegisterActivity.this, aBoolean ? "Success" : "Error", Toast.LENGTH_SHORT).show();
			}
		});

		viewModel.getLiveDataExist().observe(this, new Observer<Boolean>() {;
			@Override
			public void onChanged(Boolean exist) {
				if (exist) {
					etUsername.setError("Username already exists");
				} else {
					etUsername.setError(null);
					registerUser();
				}
			}
		});
	}

	public void setValidation() {
		Validator.clear();
		Validator.add(new Rule(etUsername, RuleOperation.REQUIRED, "Username is required"));
		Validator.add(new NameRule(etUsername, RuleOperation.NAME, "Username is not valid"));
		Validator.add(new Rule(etPassword, RuleOperation.REQUIRED, "Password is required"));
		Validator.add(new Rule(etConfirmPassword, RuleOperation.REQUIRED, "Confirm password is required"));
		Validator.add(new CompareRule(etConfirmPassword, etPassword, RuleOperation.COMPARE, "Passwords do not match"));
	}

	public boolean validate() {
		setValidation();
		return Validator.validate();
	}
}