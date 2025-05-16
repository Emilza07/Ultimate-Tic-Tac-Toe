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
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emil_z.helper.AlertUtil;
import com.emil_z.helper.BitMapHelper;
import com.emil_z.helper.Global;
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

/**
 * Activity that displays the user's profile, including profile picture, username, ELO rating,
 * and a paginated list of recent games. Allows updating the profile picture via camera or gallery,
 * and supports cropping the selected image.
 */
public class ProfileActivity extends BaseActivity {
	private final int PAGE_SIZE = 10;

	private ImageView ivPfp;
	private TextView tvUsername;
	private TextView tvElo;
	private RecyclerView rvGames;

	private GamesViewModel gamesViewModel;
	private UsersViewModel usersViewModel;
	private GamesAdapter adapter;

	private ActivityResultLauncher<Void> cameraLauncher;
	private ActivityResultLauncher<Intent> galleryLauncher;
	private ActivityResultLauncher<String> requestPermissionLauncher;

	private Games games;
	private boolean isLoading = false;
	private String lastLoadedGameId = null;

	/**
	 * Initializes the profile activity, sets up UI, listeners, ViewModels, adapter, and launchers.
	 *
	 * @param savedInstanceState The previously saved instance state, if any.
	 */
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
		setupRecyclerViewScrollListener();
		setViewModel();
		setAdapter();
		registerLaunchers();
	}

	/**
	 * Initializes view components for the profile screen.
	 */
	@Override
	public void initializeViews() {
		ivPfp = findViewById(R.id.ivPfp);
		tvUsername = findViewById(R.id.tvUsername);
		tvElo = findViewById(R.id.tvElo);
		rvGames = findViewById(R.id.rvGames);

		ivPfp.setImageBitmap(currentUser.getPictureBitmap());
		tvUsername.setText(currentUser.getUsername());
		tvElo.setText(getString(R.string.elo_format, Math.round(currentUser.getElo())));

		DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.dividor));
		rvGames.addItemDecoration(divider);
	}

	/**
	 * Sets up click listeners for profile picture.
	 */
	@Override
	protected void setListeners() {
		ivPfp.setOnClickListener(v -> showImageOptions());
	}

	/**
	 * Sets up a scroll listener for the RecyclerView to load more games when reaching the end.
	 */
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
					&& totalItemCount >= PAGE_SIZE) {
					// Load more games
					loadGames(true);
				}
			}
		});
	}

	/**
	 * Initializes ViewModels, loads games, and observes user and games data changes.
	 */
	@Override
	protected void setViewModel() {
		gamesViewModel = new ViewModelProvider(this, new GamesViewModelFactory(getApplication(), GameType.ONLINE)).get(GamesViewModel.class); //TODO: think about right game type
		usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
		loadGames(false);

		usersViewModel.getLiveDataSuccess().observe(this, success -> {
			if (success) {
				ivPfp.setImageBitmap(currentUser.getPictureBitmap());
				setResult(RESULT_OK);
			} else {
				usersViewModel.get(currentUser.getIdFs());
			}
		});

		usersViewModel.getLiveDataEntity().observe(this, user -> {
			if (user != null) {
				for (Game game : games) {
					if (game == null) continue;
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
			if (adapter.getItems() != null) {
				int lastIndex = adapter.getItems().indexOf(null);
				if (lastIndex != -1) {
					adapter.getItems().remove(lastIndex);
					adapter.notifyItemRemoved(lastIndex);
				}
			}

			if (!newGames.isEmpty()) {
				if (this.games == null)
					this.games = newGames;
				else
					this.games.addAll(newGames);
				isLoading = false;
				lastLoadedGameId = newGames.get(newGames.size() - 1).getIdFs();
				for (Game game : newGames) {
					usersViewModel.get(Objects.equals(currentUser.getIdFs(),
						game.getPlayer1().getIdFs()) ? game.getPlayer2().getIdFs() :
						game.getPlayer1().getIdFs());
				}
			}
		});
	}

	/**
	 * Sets up the adapter for the games RecyclerView and handles item clicks.
	 */
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
			Intent intent = new Intent(this, GameActivity.class);
			intent.putExtra(MainActivity.EXTRA_GAME_IDFS, item.getIdFs());
			intent.putExtra(MainActivity.EXTRA_GAME_TYPE, GameType.HISTORY);
			startActivity(intent);
		});
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
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(
							getContentResolver(),
							result.getData().getData()
						);
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
	 * Displays a dialog or options for the user to choose between taking a new profile picture
	 * using the camera or selecting one from the gallery. Handles permission requests as needed.
	 */
	private void showImageOptions() {
		Global.takePicture(this, cameraLauncher, galleryLauncher, requestPermissionLauncher);
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
	 * Loads games for the user, paginated. Shows loading indicator if loading more.
	 *
	 * @param loadMore Whether to load more games (pagination).
	 */
	private void loadGames(boolean loadMore) {
		isLoading = true;
		if (loadMore) {
			showLoadingMore();
		}
		gamesViewModel.getUserGamesPaginated(currentUser.getIdFs(), PAGE_SIZE, lastLoadedGameId);
	}

	/**
	 * Shows a loading indicator in the games list.
	 */
	private void showLoadingMore() {
		adapter.getItems().add(null);
		adapter.notifyItemInserted(adapter.getItemCount() - 1);
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