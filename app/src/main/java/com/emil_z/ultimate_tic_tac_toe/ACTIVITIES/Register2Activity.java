package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emil_z.helper.AlertUtil;
import com.emil_z.helper.BitMapHelper;
import com.emil_z.helper.Global;
import com.emil_z.helper.TextInputLayoutUtil;
import com.emil_z.helper.inputValidators.NameRule;
import com.emil_z.helper.inputValidators.Rule;
import com.emil_z.helper.inputValidators.RuleOperation;
import com.emil_z.helper.inputValidators.Validator;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.UsersViewModel;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity that handles the second part of user registration.
 * <p>
 * Handles profile picture selection, username input, and registration logic.
 */
public class Register2Activity extends BaseActivity {

	private ImageView ivPfp;
	private EditText etUsername;
	private Button btnRegister;
	private Button btnBack;

	private UsersViewModel viewModel;

	private ActivityResultLauncher<Void> cameraLauncher;
	private ActivityResultLauncher<Intent> galleryLauncher;
	private ActivityResultLauncher<String> requestPermissionLauncher;

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
		setContentView(R.layout.activity_register2);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		initializeViews();
		setListeners();
		setViewModel();
		registerLaunchers();
	}

	/**
	 * Initializes view components for registration.
	 */
	@Override
	protected void initializeViews() {
		ivPfp = findViewById(R.id.ivPfp);
		etUsername = findViewById(R.id.etEmail);
		btnRegister = findViewById(R.id.btnRegister);
		btnBack = findViewById(R.id.btnBack);
	}

	/**
	 * Sets up click listeners for register and back buttons.
	 */
	@Override
	protected void setListeners() {
		ivPfp.setOnClickListener(v -> showImageOptions());
		btnRegister.setOnClickListener(v -> {
			if (validate()) {
				showProgressDialog("Creating account...", "Please wait");
				Intent intent = getIntent();
				String email = intent.getStringExtra(Register1Activity.EXTRA_EMAIL);
				String password = intent.getStringExtra(Register1Activity.EXTRA_PASSWORD);
				Bitmap bitmap = ((BitmapDrawable) ivPfp.getDrawable()).getBitmap();
				String encodedImage = BitMapHelper.encodeTobase64(bitmap);
				viewModel.register(email, etUsername.getText().toString(), password, encodedImage);
			}
		});
		btnBack.setOnClickListener(v -> finish());
	}

	/**
	 * Sets up the ViewModel and observes LiveData for registration results.
	 */
	@Override
	protected void setViewModel() {
		viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

		viewModel.getLiveDataEntity().observe(this, user -> {
			BaseActivity.currentUser = user;
			if (user != null) {
				hideProgressDialog();
				Intent intent = new Intent(Register2Activity.this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
			}
		});

		viewModel.getLiveDataSuccess().observe(this, success -> {
			if (!success) {
				hideProgressDialog();
				Intent intent = new Intent(Register2Activity.this, LoginActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
	}

	/**
	 * Sets up validation rules for registration fields.
	 */
	public void setValidation() {
		Validator.clear();
		Validator.add(new Rule(etUsername, RuleOperation.REQUIRED, getString(R.string.no_username)));
		Validator.add(new NameRule(etUsername, RuleOperation.TEXT, getString(R.string.invalid_username)));
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
		return isValid;
	}

	/**
	 * Displays a dialog or options for the user to choose between taking a new profile picture
	 * using the camera or selecting one from the gallery. Handles permission requests as needed.
	 */
	private void showImageOptions() {
		Global.takePicture(this, cameraLauncher, galleryLauncher, requestPermissionLauncher);
	}

	/**
	 * Registers activity result launchers for camera and gallery image selection.
	 */
	private void registerLaunchers() {
		cameraLauncher = registerForActivityResult(
			new ActivityResultContracts.TakePicturePreview(),
			bitMap -> {
				if (bitMap != null)
					processNewProfileImage(bitMap);
			});

		galleryLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == RESULT_OK && result.getData() != null) {
					try {
						Bitmap bitmap;
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
							ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), result.getData().getData());
							bitmap = ImageDecoder.decodeBitmap(source);
						} else {
							bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getData().getData());
						}
						processNewProfileImage(bitmap);
					} catch (Exception e) {
						Toast.makeText(this, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show();
					}
				}
			}
		);

		requestPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(),
			isGranted -> {
				if (isGranted) {
					if (Global.getCurrentRequestType() == 0)
						cameraLauncher.launch(null);
					else
						galleryLauncher.launch(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"));
				} else {
					AlertUtil.alertOk(
						this,
						getString(R.string.permission_required),
						getString(R.string.permission_required_message),
						true,
						0
					);
				}
			}
		);
	}

	/**
	 * Processes a new profile image, resizes it, and starts the crop activity.
	 *
	 * @param bitmap The new profile image bitmap.
	 */
	private void processNewProfileImage(Bitmap bitmap) {
		try {
			File outputDir = getCacheDir();
			File outputFile = File.createTempFile("temp_image", ".jpg", outputDir);

			Bitmap resizedBitmap = getResizedBitmap(bitmap);
			FileOutputStream fos = new FileOutputStream(outputFile);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
			fos.close();

			startCropActivity(Uri.fromFile(outputFile));
		} catch (IOException e) {
			Toast.makeText(this, R.string.failed_to_load_image, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Resizes a bitmap to a maximum dimension of 512px, maintaining aspect ratio.
	 *
	 * @param image The original bitmap.
	 * @return The resized bitmap.
	 */
	private Bitmap getResizedBitmap(Bitmap image) {
		int width = image.getWidth();
		int height = image.getHeight();

		float bitmapRatio = (float) width / (float) height;
		if (bitmapRatio > 1) {
			width = 512;
			height = (int) (width / bitmapRatio);
		} else {
			height = 512;
			width = (int) (height * bitmapRatio);
		}

		return Bitmap.createScaledBitmap(image, width, height, true);
	}

	/**
	 * Starts the crop activity for the selected image.
	 *
	 * @param sourceUri The URI of the image to crop.
	 */
	private void startCropActivity(Uri sourceUri) {
		UCrop.Options options = new UCrop.Options();
		options.setToolbarColor(getResources().getColor(R.color.colorBackground, getTheme()));
		options.setStatusBarColor(getResources().getColor(R.color.colorBackground, getTheme()));
		options.setToolbarTitle("Crop Image");

		options.setToolbarWidgetColor(getResources().getColor(R.color.textColor, getTheme()));

		options.withAspectRatio(1, 1);

		File destinationFile = new File(getCacheDir(), "cropped_image_" + System.currentTimeMillis() + ".jpg");
		Uri destinationUri = Uri.fromFile(destinationFile);

		UCrop.of(sourceUri, destinationUri)
			.withOptions(options)
			.start(this);
	}

	/**
	 * Handles the result from the crop activity and updates the profile picture.
	 *
	 * @param requestCode The request code.
	 * @param resultCode  The result code.
	 * @param data        The intent data.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
			Uri resultUri = UCrop.getOutput(data);
			if (resultUri == null) {
				Toast.makeText(this, "Failed to load the image", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				Bitmap croppedBitmap;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
					ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), resultUri);
					croppedBitmap = ImageDecoder.decodeBitmap(source);
				} else {
					croppedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
				}
				// Now update the image view
				ivPfp.setImageBitmap(croppedBitmap);
			} catch (IOException e) {
				Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
			}
		}
	}
}