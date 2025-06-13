package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.TextInputLayoutUtil;
import com.emil_z.helper.inputValidators.CompareRule;
import com.emil_z.helper.inputValidators.EmailRule;
import com.emil_z.helper.inputValidators.PasswordRule;
import com.emil_z.helper.inputValidators.Rule;
import com.emil_z.helper.inputValidators.RuleOperation;
import com.emil_z.helper.inputValidators.Validator;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity that handles the first part of user registration.
 * <p>
 * Validates user input, checks for existing email, and navigates to the Register2 activity upon.
 */
public class Register1Activity extends BaseActivity {
	public static final String EXTRA_EMAIL = "com.emil_z.EXTRA_EMAIL";
	public static final String EXTRA_PASSWORD = "com.emil_z.PASSWORD";

	private EditText etEmail;
	private EditText etPassword;
	private EditText etConfirmPassword;
	private TextView tvError;
	private Button btnContinue;
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
		setContentView(R.layout.activity_register1);
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
		etEmail = findViewById(R.id.etEmail);
		etPassword = findViewById(R.id.etPassword);
		etConfirmPassword = findViewById(R.id.etConfirmPassword);
		tvError = findViewById(R.id.tvError);
		btnContinue = findViewById(R.id.btnRegister);
		btnBack = findViewById(R.id.btnBack);
	}

	/**
	 * Sets up click listeners for continue and back buttons.
	 */
	@Override
	protected void setListeners() {
		btnContinue.setOnClickListener(v -> {
			if (validate()) {
				showProgressDialog(getString(R.string.checking_email_availability), getString(R.string.please_wait));
				viewModel.exist(etEmail.getText().toString()); //TODO: check if username or email exists (think about option for same username on different emails)
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
			hideProgressDialog();
			if (exist) {
				etEmail.setError(getString(R.string.email_taken));
				TextInputLayoutUtil.transferErrorsToTextInputLayout(etEmail);
			} else {
				etEmail.setError(null);
				Intent intent = new Intent(Register1Activity.this, Register2Activity.class);
				intent.putExtra(EXTRA_EMAIL, etEmail.getText().toString());
				intent.putExtra(EXTRA_PASSWORD, etPassword.getText().toString());
				startActivity(intent);
			}
		});

		viewModel.getLiveDataSuccess().observe(this, success -> {
			if (success) {
				Intent intent = new Intent(Register1Activity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(this, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Sets up validation rules for registration fields.
	 */
	public void setValidation() {
		Validator.clear();
		Validator.add(new Rule(etEmail, RuleOperation.REQUIRED, getString(R.string.invalid_email)));
		Validator.add(new EmailRule(etEmail, RuleOperation.TEXT, getString(R.string.invalid_email)));
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

		if (etEmail.getError() != null)
			etEmail.setError(getString(R.string.invalid_email));
		TextInputLayoutUtil.transferErrorsToTextInputLayout(etEmail);
		TextInputLayout til = TextInputLayoutUtil.getTextInputLayout(etPassword);
		boolean hasPasswordError = etPassword.getError() != null;
		til.setError(hasPasswordError ? " " : null);
		etPassword.setError(null);
		tvError.setVisibility(hasPasswordError ? TextView.VISIBLE : TextView.INVISIBLE);

		TextInputLayoutUtil.transferErrorsToTextInputLayout(etConfirmPassword);
		return isValid;
	}
}