package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.PreferenceManager;
import com.emil_z.helper.inputValidators.Rule;
import com.emil_z.helper.inputValidators.RuleOperation;
import com.emil_z.helper.inputValidators.Validator;
import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;

public class LoginActivity extends BaseActivity {
	private EditText etUsername;
	private EditText etPassword;
	private CheckBox cbRememberMe;
	private Button btnSignIn;
	private Button btnBack;

	private UsersViewModel viewModel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_login);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setListeners();
		setViewModel();	}

	protected void initializeViews() {
		etUsername = findViewById(R.id.etUsername);
		etPassword = findViewById(R.id.etPassword);
		cbRememberMe = findViewById(R.id.cbRememberMe);
		btnSignIn = findViewById(R.id.btnSignIn);
		btnBack = findViewById(R.id.btnBack);
	}

	protected void setListeners() {
		btnSignIn.setOnClickListener(v -> {
			if(validate()){
				String username = etUsername.getText().toString();
				String password = etPassword.getText().toString();
				viewModel.logIn(username, password);
			}
		});
		btnBack.setOnClickListener(v -> {
			finish();
		});
	}

	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		viewModel.getSuccess().observe(this, new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean success) {
				if (!success){
					Toast.makeText(LoginActivity.this, "Username or password is incorrect", Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewModel.getEntity().observe(this, new Observer<User>() {
			@Override
			public void onChanged(User user) {
				if (user != null){
					BaseActivity.currentUser = user;
					if(cbRememberMe.isChecked())
						PreferenceManager.writeToSharedPreferences(LoginActivity.this, "user_prefs", new Object[][] {{"UserIdFs", user.getIdFs(), "String"}, {"Username", user.getUsername(), "String"}, {"Password", user.getPassword(), "String"}});
					Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
				}
			}
		});
	}

	public void setValidation(){
		Validator.clear();
		Validator.add(new Rule(etUsername, RuleOperation.REQUIRED, "Please enter username"));
		Validator.add(new Rule(etPassword, RuleOperation.REQUIRED, "Please enter password"));
	}

	public boolean validate(){
		setValidation();
		return Validator.validate();
	}
}