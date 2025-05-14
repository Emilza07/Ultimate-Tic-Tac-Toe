package com.emil_z.ultimate_tic_tac_toe.ACTIVITIES;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.model.Game;
import com.emil_z.model.GameType;
import com.emil_z.model.Games;
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
	private final int pageSize = 10;
	private ImageView ivPfp;
	private TextView tvUsername;
	private TextView tvElo;
	private RecyclerView rvGames;
	private GamesViewModel gamesViewModel;
	private UsersViewModel usersViewModel;
	private GamesAdapter adapter;
	private ActivityResultLauncher<Intent> cameraLauncher;
	private ActivityResultLauncher<Intent> galleryLauncher;
	private Games games;
	private boolean isLoading = false;
	private String lastVisibleGameId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
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
		setupRecyclerViewScrollListener();
		registerLaunchers();

	}

	@Override
	public void initializeViews() {
		ivPfp = findViewById(R.id.ivPfp);
		tvUsername = findViewById(R.id.tvUsername);
		tvElo = findViewById(R.id.tvElo);
		rvGames = findViewById(R.id.rvGames);

		ivPfp.setImageBitmap(currentUser.getPictureBitmap());
		tvUsername.setText(currentUser.getUsername());
		tvElo.setText(getString(R.string.elo_format, Math.round(currentUser.getElo())));

		DividerItemDecoration divider = new DividerItemDecoration(rvGames.getContext(), LinearLayoutManager.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.dividor));
		rvGames.addItemDecoration(divider);
	}

	@Override
	protected void setListeners() {
		ivPfp.setOnClickListener(v -> showImageOptions());
	}

	private void showImageOptions() {
		String[] options = {getString(R.string.profile_image_camera), getString(R.string.profile_image_gallery)};
		new AlertDialog.Builder(this)
				.setTitle(R.string.profile_image_dialog_title)
				.setItems(options, (dialog, which) -> {
					if (which == 0) {
						Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						cameraLauncher.launch(intent);
					} else {
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
							if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
									!= PackageManager.PERMISSION_GRANTED) {
								requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 100);
							} else {
								launchGalleryPicker();
							}
						} else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
								!= PackageManager.PERMISSION_GRANTED) {
							requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
						} else {
							launchGalleryPicker();
						}
					}
				})
				.show();
	}

	private void launchGalleryPicker() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		// Optional: add a title for the picker
		if (intent.resolveActivity(getPackageManager()) != null) {
			galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"));
		} else {
			Toast.makeText(this, "No gallery app available", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 100) {
			if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
				launchGalleryPicker();
			} else {
				Toast.makeText(this, "Storage permission is required to select images", Toast.LENGTH_SHORT).show();
			}
		}
	}
	@Override
	protected void setViewModel() {
		gamesViewModel = new ViewModelProvider(this, new GamesViewModelFactory(getApplication(), GameType.ONLINE)).get(GamesViewModel.class); //TODO: think about right game type
		usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		loadGames(false);

		usersViewModel.getLiveDataSuccess().observe(this, success -> {
			if (success) {
				ivPfp.setImageBitmap(currentUser.getPictureBitmap());// Convert bitmap to base64 and save to user profile
				setResult(RESULT_OK);
			} else {
				usersViewModel.get(currentUser.getIdFs());
			}
		});

		usersViewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null && Objects.equals(user.getIdFs(), currentUser.getIdFs())) {
				currentUser = user;
			} else if (user != null) {
				for (Game game : games) {
					if (Objects.equals(currentUser.getIdFs(), game.getPlayer1().getIdFs())) {
						game.getPlayer2().setPicture(user.getPicture());
					} else {
						game.getPlayer1().setPicture(user.getPicture());
					}
				}
			}
			adapter.setItems(this.games);
		});

		gamesViewModel.getLiveDataCollection().observe(this, newGames -> {
			if (!newGames.isEmpty()) {
				if (this.games == null) {
					this.games = newGames;
				} else if (isLoading) {
					// For subsequent loads, add to existing games
					this.games.addAll(newGames);
				}

				isLoading = false;

				// If we got less than pageSize, there are no more games to load
				if (!newGames.isEmpty()) {
					lastVisibleGameId = newGames.get(newGames.size() - 1).getIdFs();
				}

				// Process games (get user profiles)
				for (Game game : newGames) {
					usersViewModel.get(Objects.equals(currentUser.getIdFs(),
							game.getPlayer1().getIdFs()) ? game.getPlayer2().getIdFs() :
							game.getPlayer1().getIdFs());
				}
			}
		});
	}

	public void setAdapter() {
		adapter = new GamesAdapter(null,
				R.layout.game_single_layout,
				holder -> {
					holder.putView("ivPfp", holder.itemView.findViewById(R.id.ivPfp));
					holder.putView("tvUsername", holder.itemView.findViewById(R.id.tvUsername));
					holder.putView("tvElo", holder.itemView.findViewById(R.id.tvElo));
					holder.putView("ivGameResult", holder.itemView.findViewById(R.id.ivGameResult));
				},
				((holder, item, position) -> {
					Player opponent = (Objects.equals(item.getPlayer1().getIdFs(), currentUser.getIdFs()) ? item.getPlayer2() : item.getPlayer1());
					((ImageView) holder.getView("ivPfp")).setImageBitmap(opponent.getPictureBitmap());
					((TextView) holder.getView("tvUsername")).setText(opponent.getName());
					((TextView) holder.getView("tvElo")).setText(getString(R.string.player_elo_format, Math.round(opponent.getElo())));
					if (Objects.equals(item.getWinnerIdFs(), currentUser.getIdFs()))
						((ImageView) holder.getView("ivGameResult")).setImageResource(R.drawable.checkmark);
					else if (Objects.equals(item.getWinnerIdFs(), "T"))
						((ImageView) holder.getView("ivGameResult")).setImageResource(R.drawable.tie);
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

	private void setupRecyclerViewScrollListener() {
		rvGames.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);

				LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
				int visibleItemCount = layoutManager.getChildCount();
				int totalItemCount = layoutManager.getItemCount();
				int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

				if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
						&& firstVisibleItemPosition >= 0
						&& totalItemCount >= pageSize) {
					// Load more games
					loadMoreGames();
				}
			}
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

	private void loadGames(boolean loadMore) {
		isLoading = true;
		if (loadMore) {
			// Show loading indicator at the bottom
			showLoadingMore();
		}
		gamesViewModel.getUserGamesPaginated(currentUser.getIdFs(), pageSize, lastVisibleGameId);
	}

	private void loadMoreGames() {
		loadGames(true);
	}

	private void showLoadingMore() {
		// You can add a progress bar at the bottom of the list or show a toast
		// For example:
		Toast.makeText(this, "Loading more games...", Toast.LENGTH_SHORT).show();
	}

	private void processNewProfileImage(Bitmap bitmap) {
		// Create a temporary file to save the image
		try {
			File outputDir = getCacheDir();
			File outputFile = File.createTempFile("temp_image", ".jpg", outputDir);

			// Save bitmap to the file
			Bitmap resizedBitmap = getResizedBitmap(bitmap, 512);
			FileOutputStream fos = new FileOutputStream(outputFile);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
			fos.close();

			// Start the cropping activity
			startCropActivity(Uri.fromFile(outputFile));
		} catch (IOException e) {
			Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
		}
	}

	private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
		int width = image.getWidth();
		int height = image.getHeight();

		float bitmapRatio = (float) width / (float) height;
		if (bitmapRatio > 1) {
			width = maxSize;
			height = (int) (width / bitmapRatio);
		} else {
			height = maxSize;
			width = (int) (height * bitmapRatio);
		}

		return Bitmap.createScaledBitmap(image, width, height, true);
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
		File destinationFile = new File(getCacheDir(), "cropped_image_" + System.currentTimeMillis() + ".jpg");
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
				ivPfp.setImageBitmap(croppedBitmap);
				String base64Image = BitMapHelper.encodeTobase64(croppedBitmap);
				currentUser.setPicture(base64Image);
				usersViewModel.update(currentUser);
			} catch (IOException e) {
				Toast.makeText(this, "Failed to load cropped image", Toast.LENGTH_SHORT).show();
			}
		}
	}
}