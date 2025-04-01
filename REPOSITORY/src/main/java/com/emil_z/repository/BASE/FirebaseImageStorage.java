package com.emil_z.repository.BASE;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import com.emil_z.helper.BitMapHelper;
import com.emil_z.helper.StringUtil;


public class FirebaseImageStorage {
    private static final String     TAG = "FirebaseImageStorage";
    private static FirebaseStorage  storage;
    private static StorageReference reference;

    public static void initialize(Context context) {
        if (storage == null) {
            try {
                storage = FirebaseStorage.getInstance();
            } catch (Exception ex) {
                FirebaseInstance instance = FirebaseInstance.instance(context);
                storage = FirebaseStorage.getInstance(FirebaseInstance.app);
            }
        }

        if (reference == null)
            reference = storage.getReference();
    }

    public static Task<String> saveToStorage(String picture, String id, String path) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        if (StringUtil.isNullOrEmpty(picture)) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitMapHelper.decodeBase64(picture).compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        UploadTask uploadTask = getReference(id + ".jpg", path).putBytes(imageData);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl()
                    .addOnSuccessListener(uri -> tcs.setResult(uri.toString()))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                        tcs.setResult(null);
                    });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload image: " + e.getMessage());
            tcs.setResult(null);
        });

        return tcs.getTask();
    }

    public static Task<String> loadFromStorage(String id, String path) {
        TaskCompletionSource<String> completionPicture = new TaskCompletionSource<>();

        int maxDownloadSizeBytes = 1024 * 1024;

        getReference(id + ".jpg", path).getBytes(maxDownloadSizeBytes)
                .addOnSuccessListener(bytes -> completionPicture.setResult(BitMapHelper.encodeTobase64(bytes)))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load image: " + e.getMessage());
                    completionPicture.setResult(null);
                });

        return completionPicture.getTask();
    }

    public static Task<Boolean> deleteFromStorage(String id, String path) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        getReference(id + ".jpg", path).delete()
                .addOnSuccessListener(unused -> tcs.setResult(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete image: " + e.getMessage());
                    tcs.setResult(false);
                });

        return tcs.getTask();
    }

    private static StorageReference getReference(String id, String path) {
        StorageReference ref = storage.getReference();

        if (path != null) {
            ref = makePath(ref, path);
        }

        return ref.child(id);
    }

    private static StorageReference makePath(StorageReference reference, String path) {
        if (path != null) {
            String[] vs = path.split("[\\\\/]");
            for (String v : vs) {
                reference = reference.child(v);
            }
        }
        return reference;
    }
}