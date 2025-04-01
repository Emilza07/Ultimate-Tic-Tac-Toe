package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.inputValidators.CompareRule;
import com.emil_z.helper.inputValidators.EmailRule;
import com.emil_z.helper.inputValidators.Rule;
import com.emil_z.helper.inputValidators.RuleOperation;
import com.emil_z.helper.inputValidators.Validator;
import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

public class RegisterActivity extends AppCompatActivity {
	private EditText etUsername;
	private EditText etEmail;
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
		setValidation();
	}
	protected void initializeViews() {
		etUsername = findViewById(R.id.etUsername);
		etEmail = findViewById(R.id.etEmail);
		etPassword = findViewById(R.id.etPassword);
		etConfirmPassword = findViewById(R.id.etConfirmPassword);
		btnRegister = findViewById(R.id.btnRegister);
		btnBack = findViewById(R.id.btnBack);

		setListeners();
	}

	protected void setListeners() {
		btnRegister.setOnClickListener(v -> {
			// Register user
			if(validate()) {
				User user = new User(etUsername.getText().toString(),
						etEmail.getText().toString(),
						etPassword.getText().toString());
				viewModel.save(user);
			}
		});
		btnBack.setOnClickListener(v -> {
			finish();
		});

		setViewModel();
	}

	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getSuccess().observe(this, new Observer<Boolean>() {
			public void onChanged(Boolean aBoolean) {
				Toast.makeText(RegisterActivity.this, aBoolean ? "Success" : "Error", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void setValidation() {
		Validator.add(new Rule(etUsername, RuleOperation.REQUIRED, "Username is required"));
		Validator.add(new Rule(etUsername, RuleOperation.NAME, "Username is not valid"));
		Validator.add(new Rule(etEmail, RuleOperation.REQUIRED, "Email is required"));
		Validator.add(new EmailRule(etEmail, RuleOperation.TEXT, "Email is not valid"));
		Validator.add(new Rule(etPassword, RuleOperation.REQUIRED, "Password is required"));
		Validator.add(new Rule(etConfirmPassword, RuleOperation.REQUIRED, "Confirm password is required"));
		Validator.add(new CompareRule(etConfirmPassword, etPassword, RuleOperation.COMPARE, "Passwords do not match"));
	}
	public boolean validate() {
		return Validator.validate();
	}
}