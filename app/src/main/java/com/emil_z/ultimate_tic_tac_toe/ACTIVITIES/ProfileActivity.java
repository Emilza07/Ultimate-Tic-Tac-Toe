package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.GameType;
import com.emil_z.model.Player;
import com.emil_z.ultimate_tic_tac_toe.ACTIVITIES.BASE.BaseActivity;
import com.emil_z.ultimate_tic_tac_toe.ADPTERS.GamesAdapter;
import com.emil_z.ultimate_tic_tac_toe.R;
import com.emil_z.viewmodel.GamesViewModel;
import com.emil_z.viewmodel.GamesViewModelFactory;
import com.emil_z.viewmodel.UsersViewModel;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class ProfileActivity extends BaseActivity {
	private ImageView ivAvatar;
	private TextView tvUsername;
	private TextView tvElo;
	private RecyclerView rvGames;

	private GamesViewModel gamesViewModel;
	private UsersViewModel usersViewModel;
	private GamesAdapter adapter;
	private ActivityResultLauncher<Intent> cameraLauncher;
	private ActivityResultLauncher<Intent> galleryLauncher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_profile);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});

		initializeViews();
		setListeners();
		setViewModel();
		setAdapter();
		registerLaunchers();

	}

	@Override
	public void initializeViews() {
		ivAvatar = findViewById(R.id.ivAvatar);
		tvUsername = findViewById(R.id.tvUsername);
		tvElo = findViewById(R.id.tvElo);
		rvGames = findViewById(R.id.rvGames);

		ivAvatar.setImageBitmap(currentUser.getPictureBitmap());
		tvUsername.setText(currentUser.getUsername());
		tvElo.setText(getString(R.string.elo_format, Math.round(currentUser.getElo())));
	}

	@Override
	protected void setListeners() {
		ivAvatar.setOnClickListener(v -> showImageOptions());
	}

	private void showImageOptions() {
		String[] options = {"Take Photo", "Select from Gallery"}; //TODO: move to strings.xml
		new AlertDialog.Builder(this)
				.setTitle("Choose an option")
				.setItems(options, (dialog, which) -> {
					if (which == 0) {
						Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						cameraLauncher.launch(intent);
					} else {
						Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						galleryLauncher.launch(intent);
					}
				})
				.show();
	}

	@Override
	protected void setViewModel() {
		gamesViewModel = new ViewModelProvider(this, new GamesViewModelFactory(getApplication(), GameType.ONLINE)).get(GamesViewModel.class); //TODO: think about right game type
		usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		showProgressDialog("Games", "Loading games...");
		gamesViewModel.getUserGames(currentUser.getIdFs());

		usersViewModel.getLiveDataSuccess().observe(this, success -> {
			if (success) {
				ivAvatar.setImageBitmap(currentUser.getPictureBitmap());// Convert bitmap to base64 and save to user profile
				setResult(RESULT_OK);
			} else {
				usersViewModel.get(currentUser.getIdFs());
			}
		});
		usersViewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				currentUser = user;
			}
		});
		gamesViewModel.getLiveDataCollection().observe(this, games -> {
			if (games != null) {
				adapter.setItems(games);
				hideProgressDialog();
			}
		});
	}

	public void setAdapter() {
		adapter = new GamesAdapter(null,
				R.layout.game_single_layout,
				holder -> {
					holder.putView("ivAvatar", holder.itemView.findViewById(R.id.ivAvatar));
					holder.putView("tvUsername", holder.itemView.findViewById(R.id.tvUsername));
					holder.putView("tvElo", holder.itemView.findViewById(R.id.tvElo));
					holder.putView("ivGameResult", holder.itemView.findViewById(R.id.ivGameResult));
				},
				((holder, item, position) -> {
					Player opponent = (Objects.equals(item.getPlayer1().getIdFs(), currentUser.getIdFs()) ? item.getPlayer2() : item.getPlayer1());
					((ImageView) holder.getView("ivAvatar")).setImageBitmap(opponent.getPictureBitmap());
					((TextView) holder.getView("tvUsername")).setText(opponent.getName());
					((TextView) holder.getView("tvElo")).setText(getString(R.string.player_elo_format, Math.round(opponent.getElo())));
					if (Objects.equals(item.getWinnerIdFs(), currentUser.getIdFs()))
						((ImageView) holder.getView("ivGameResult")).setImageResource(R.drawable.ok);
					else if (Objects.equals(item.getWinnerIdFs(), "T"))
						((ImageView) holder.getView("ivGameResult")).setImageResource(R.drawable.t);
					else
						((ImageView) holder.getView("ivGameResult")).setImageResource(R.drawable.x);
				})
		);

		rvGames.setAdapter(adapter);
		rvGames.setLayoutManager(new LinearLayoutManager(this));

		adapter.setOnItemClickListener((item, position) -> {
			//TODO: start watching game;
			Intent intent = new Intent(this, GameActivity.class);
			intent.putExtra(getString(R.string.EXTRA_GAME_IDFS), item.getIdFs());
			intent.putExtra(getString(R.string.EXTRA_GAME_TYPE), GameType.HISTORY);
			startActivity(intent);
		});
	}

	private void registerLaunchers() {
		cameraLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (result.getResultCode() == RESULT_OK && result.getData() != null) {
						// Handle camera result - the image is usually in the "data" extra
						Bundle extras = result.getData().getExtras();
						if (extras != null && extras.containsKey("data")) {
							// Get the thumbnail bitmap
							// Note: For high quality, consider using FileProvider approach instead
							processNewProfileImage((Bitmap) extras.get("data"));
						}
					}
				});

		galleryLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				result -> {
					if (result.getResultCode() == RESULT_OK && result.getData() != null) {
						Uri selectedImage = result.getData().getData();
						if (selectedImage != null) {
							try {
								Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
								processNewProfileImage(bitmap);
							} catch (Exception ignored) {
								Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
							}
						}
					}
				});
	}

	private void processNewProfileImage(Bitmap bitmap) {
		// Create a temporary file to save the image
		try {
			File outputDir = getCacheDir();
			File outputFile = File.createTempFile("temp_image", ".png", outputDir);

			// Save bitmap to the file
			FileOutputStream fos = new FileOutputStream(outputFile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();

			// Start the cropping activity
			startCropActivity(Uri.fromFile(outputFile));
		} catch (IOException e) {
			Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
		}
	}

	private void startCropActivity(Uri sourceUri) {
		UCrop.Options options = new UCrop.Options();
		options.setToolbarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
		options.setStatusBarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
		options.setToolbarTitle("Crop Image");

		options.setToolbarWidgetColor(getResources().getColor(R.color.textColor, getTheme()));

		// Force square aspect ratio
		options.withAspectRatio(1, 1);

		// Create destination file
		File destinationFile = new File(getCacheDir(), "cropped_image_" + System.currentTimeMillis() + ".png");
		Uri destinationUri = Uri.fromFile(destinationFile);

		UCrop.of(sourceUri, destinationUri)
				.withOptions(options)
				.start(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
			Uri resultUri = UCrop.getOutput(data);
			try {
				Bitmap croppedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
				// Now update the image view and save to user profile
				ivAvatar.setImageBitmap(croppedBitmap);
				String base64Image = BitMapHelper.encodeTobase64(croppedBitmap);
				currentUser.setPicture(base64Image);
				usersViewModel.update(currentUser);
			} catch (IOException e) {
				Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
			}
		}
	}
}