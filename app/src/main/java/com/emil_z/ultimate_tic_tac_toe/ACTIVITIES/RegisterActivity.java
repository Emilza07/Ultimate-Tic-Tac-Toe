package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.helper.PasswordUtil;
import com.emil_z.helper.TextInputLayoutUtil;
import com.emil_z.helper.inputValidators.CompareRule;
import com.emil_z.helper.inputValidators.NameRule;
import com.emil_z.helper.inputValidators.PasswordRule;
import com.emil_z.helper.inputValidators.Rule;
import com.emil_z.helper.inputValidators.RuleOperation;
import com.emil_z.helper.inputValidators.Validator;
import com.emil_z.model.User;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity that handles user registration.
 * <p>
 * Validates user input, checks for existing usernames, and saves new user data.
 */
public class RegisterActivity extends BaseActivity {
	private EditText etUsername;
	private EditText etPassword;
	private EditText etConfirmPassword;
	private TextView tvError;
	private Button btnRegister;
	private Button btnBack;

	private UsersViewModel viewModel;

	/**
	 * Initializes the registration activity, sets up the UI,
	 * and prepares listeners and ViewModel.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
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

	/**
	 * Initializes view components for registration.
	 */
	@Override
	protected void initializeViews() {
		etUsername = findViewById(R.id.etUsername);
		etPassword = findViewById(R.id.etPassword);
		etConfirmPassword = findViewById(R.id.etConfirmPassword);
		tvError = findViewById(R.id.tvError);
		btnRegister = findViewById(R.id.btnRegister);
		btnBack = findViewById(R.id.btnBack);
	}

	/**
	 * Sets up click listeners for registration and back buttons.
	 */
	@Override
	protected void setListeners() {
		btnRegister.setOnClickListener(v -> {
			if (validate()) {
				viewModel.exist(etUsername.getText().toString());
			}
		});
		btnBack.setOnClickListener(v -> finish());

	}

	/**
	 * Initializes the ViewModel and observes username existence.
	 */
	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataExist().observe(this, exist -> {
			if (exist) {
				etUsername.setError(getString(R.string.username_taken));
				TextInputLayoutUtil.transferErrorsToTextInputLayout(etUsername);
			} else {
				etUsername.setError(null);
				registerUser();
			}
		});
	}

	/**
	 * Registers a new user and navigates to the login screen.
	 */
	protected void registerUser() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_pfp);
		String hashedPassword = PasswordUtil.hashPassword(etPassword.getText().toString());
		User user = new User(etUsername.getText().toString(),
			hashedPassword,
			BitMapHelper.encodeTobase64(bitmap));
		viewModel.save(user);
		Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * Sets up validation rules for registration fields.
	 */
	public void setValidation() {
		Validator.clear();
		Validator.add(new Rule(etUsername, RuleOperation.REQUIRED, getString(R.string.no_username)));
		Validator.add(new NameRule(etUsername, RuleOperation.NAME, getString(R.string.username_invalid)));
		Validator.add(new Rule(etPassword, RuleOperation.REQUIRED, getString(R.string.no_password)));
		Validator.add(new PasswordRule(etPassword, RuleOperation.PASSWORD, getString(R.string.password_invalid), 8, 64));
		Validator.add(new Rule(etConfirmPassword, RuleOperation.REQUIRED, getString(R.string.no_confirm_password)));
		Validator.add(new CompareRule(etConfirmPassword, etPassword, RuleOperation.COMPARE, getString(R.string.passwords_dont_match)));
	}

	/**
	 * Validates registration fields and updates error messages.
	 *
	 * @return true if all fields are valid, false otherwise.
	 */
	public boolean validate() {
		setValidation();
		boolean isValid = Validator.validate();

		TextInputLayoutUtil.transferErrorsToTextInputLayout(etUsername);
		TextInputLayout til = TextInputLayoutUtil.getTextInputLayout(etPassword);
		boolean hasPasswordError = etPassword.getError() != null;
		til.setError(hasPasswordError ? " " : null);
		etPassword.setError(null);
		tvError.setVisibility(hasPasswordError ? TextView.VISIBLE : TextView.INVISIBLE);

		TextInputLayoutUtil.transferErrorsToTextInputLayout(etConfirmPassword);
		return isValid;
	}
}