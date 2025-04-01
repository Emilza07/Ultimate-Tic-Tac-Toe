package com.emil_z.helper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

public class Global {
    private static int currentRequestType = 0; // 0 for camera, 1 for gallery

    public static int getCurrentRequestType() {
        return currentRequestType;
    }

    /*
    How to use takePicture in the Activity

    1. Declare the launchers
    private ActivityResultLauncher<Void>   cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    // This launcher is needed if you need to check the permission
    private ActivityResultLauncher<String> requestPermissionLauncher;

    2. Set the launchers in the onCreate method
    private void setLaunchers(){
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitMap -> {
                    if (bitMap != null){
                        bitmapPhoto = bitMap;
                        ivMember.setImageBitmap(bitmapPhoto);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null){
                        final Uri imageUri = result.getData().getData();
                        try {
                            bitmapPhoto= BitMapHelper.uriToBitmap(imageUri, MemberActivity.this);
                            ivMember.setImageBitmap(bitmapPhoto);
                        }
                        catch (Exception e) {}
                    }
                });

        // This launcher is needed if you need to check the permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Launch appropriate action based on currentRequestType
                        if (Global.getCurrentRequestType() == 0) {
                            cameraLauncher.launch(null);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            galleryLauncher.launch(intent);
                        }
                    } else {
                        AlertUtil.alertOk(MemberActivity.this, "Permission required", "Permission required to access camera/gallery", true, 0);
                    }
                });

        //    For permission laucher for camera and gallery - add to Manifest.xml
        //    <uses-permission android:name="android.permission.CAMERA" />
        //    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
        //    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

    }

    3. Usage
    a. Global.takePicture(YourActivity.this, cameraLauncher, galleryLauncher);
       Shows dialog with buttons
       parameters:
       YourActivity.this - Name of the Activity
       cameraLauncher - launcher for camera
       galleryLauncher - launcher for gallery

    b. Global.takePicture(YourActivity.this, false, cameraLauncher, galleryLauncher);
       parameters:
       YourActivity.this - Name of the Activity
       showList - true: show dialog as list, false: show dialog as buttons
       cameraLauncher - launcher for camera
       galleryLauncher - launcher for gallery

    c. Global.takePicture(YourActivity.this, true, cameraLauncher, galleryLauncher, requestPermissionLauncher);
       parameters:
       YourActivity.this - Name of the Activity
       showList - true: show dialog as list, false: show dialog as buttons
       cameraLauncher - launcher for camera
       galleryLauncher - launcher for gallery
       requestPermissionLauncher - launcher for permission

    d. Global.takePicture(YourActivity.this, cameraLauncher, galleryLauncher, requestPermissionLauncher);
       Shows dialog with buttons
       parameters:
       YourActivity.this - Name of the Activity
       cameraLauncher - launcher for camera
       galleryLauncher - launcher for gallery
       requestPermissionLauncher - launcher for permission
    */

    public static void takePicture(Context context,
                                   ActivityResultLauncher<Void> cameraLauncher,
                                   ActivityResultLauncher<Intent> galleryLauncher) {

        takePicture(context, false, cameraLauncher, galleryLauncher, null);
    }

    public static void takePicture(Context context, boolean showList,
                                   ActivityResultLauncher<Void> cameraLauncher,
                                   ActivityResultLauncher<Intent> galleryLauncher) {

        takePicture(context, showList, cameraLauncher, galleryLauncher, null);
    }

    public static void takePicture(Context context,
                                   ActivityResultLauncher<Void> cameraLauncher,
                                   ActivityResultLauncher<Intent> galleryLauncher,
                                   ActivityResultLauncher<String> permissionLauncher) {

        takePicture(context, false, cameraLauncher, galleryLauncher, permissionLauncher);
    }

    public static void takePicture(Context context, boolean showList,
                                   ActivityResultLauncher<Void> cameraLauncher,
                                   ActivityResultLauncher<Intent> galleryLauncher,
                                   ActivityResultLauncher<String> permissionLauncher) {

        String[] items = {"Take Photo", "Choose from Library", "Cancel"};

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Photo")
                //.setMessage("Select photo from")  // don't work wit list
                .setCancelable(true);

        if (showList) {
            builder.setItems((items), (dialog, which) -> {
                // Take photo
                if (which == 0) {
                    takeCameraPicture(context, cameraLauncher, permissionLauncher);
                }
                // Choose from gallery
                else if (which == 1) {
                    chooseFromGallery(context, galleryLauncher, permissionLauncher);
                }
            });
        } else {
            builder.setMessage("Select photo from");
            builder.setPositiveButton("Camera", (dialog, which) -> {
                takeCameraPicture(context, cameraLauncher, permissionLauncher);
            });
            builder.setNegativeButton("Gallery", (dialog, which) -> {
                chooseFromGallery(context, galleryLauncher, permissionLauncher);
            });
            builder.setNeutralButton("Cancel", null);
        }

        builder.show();
    }

    private static void takeCameraPicture(Context context, ActivityResultLauncher<Void> cameraLauncher, ActivityResultLauncher<String> permissionLauncher) {
        currentRequestType = 0;

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null);
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private static void chooseFromGallery(Context context, ActivityResultLauncher<Intent> galleryLauncher, ActivityResultLauncher<String> permissionLauncher) {
        currentRequestType = 1;

        if (permissionLauncher == null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        } else {
            // Check gallery permission based on Android version
            String permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            }

            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            } else {
                permissionLauncher.launch(permission);
            }
        }
    }
}