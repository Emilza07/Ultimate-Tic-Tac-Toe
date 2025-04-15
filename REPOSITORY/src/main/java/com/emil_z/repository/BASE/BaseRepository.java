package com.emil_z.repository.BASE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.emil_z.helper.StringUtil;
import com.emil_z.model.BASE.BaseEntity;
import com.emil_z.model.BASE.BaseList;
import com.google.firebase.functions.FirebaseFunctions;


public abstract class BaseRepository<TEntity extends BaseEntity, TCollection extends BaseList<TEntity, TCollection>> {
    private static final String                 TAG = "BaseRepository";
    @SuppressLint("StaticFieldLeak")
    private static FirebaseFirestore            db;
    private Class<TEntity>                      typeEntity;
    private Class<TCollection>                  typeCollection;
    private TaskCompletionSource<Boolean>       tcSuccess;
    private TaskCompletionSource<TEntity>       tcEntity;
    private CollectionReference                 collection;
    private ListenerRegistration listenerRegistration = null;
    protected static FirebaseFunctions          function;


    public BaseRepository() { }

    public BaseRepository(Class<TEntity> tEntity, Class<TCollection> tCollection, Context context)
    {
        typeEntity     = tEntity;
        typeCollection = tCollection;

        db         = getDb(context);
        collection = db.collection(getCollectionName());
        function   = getFunction(context);
        FirebaseImageStorage.initialize(context);
    }
    protected CollectionReference getCollectionReference() {
        return collection;
    }
    private static FirebaseFunctions getFunction(Context context) {
        if (function == null) {
            try {
                function = FirebaseFunctions.getInstance();
            } catch (Exception e) {
                FirebaseInstance instance = FirebaseInstance.instance(context);
                function = FirebaseFunctions.getInstance(FirebaseInstance.app);
            }
        }
        return function;
    }
    private static FirebaseFirestore getDb(Context context) {
        if (db == null) {
            try {
                db = FirebaseFirestore.getInstance();
            } catch (Exception e) {
                FirebaseInstance instance = FirebaseInstance.instance(context);
                db = FirebaseFirestore.getInstance(FirebaseInstance.app);
            }
        }
        return db;
    }

    //region INITIALIZE
    public  Class<TEntity> getEntityType() {
        return typeEntity;
    }

    public String getEntityName(){
        return typeEntity.getSimpleName();
    }

    public Class<TCollection> getCollectionType() {
        return typeCollection;
    }

    public CollectionReference getCollection() { return collection; }

    public String getCollectionName(){
        return typeCollection.getSimpleName();
    }
    //endregion

    protected abstract Query getQueryForExist(TEntity entity);

    /*
    public abstract Task<Boolean> exist(TEntity entity) ;
     */

    public Task<Boolean> exist(TEntity entity) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        Query query = getQueryForExist(entity);

        if (query != null) {
            get(query)
                    .addOnSuccessListener(savedEntity -> {
                        int time = 1;
                        Log.d("qqq", "Entering addOnSuccessListener");
                        Log.d("qqq", "saveEntity is null ? " + ((savedEntity == null) ? "YES" : "NO"));
                        if (savedEntity == null) {
                            Log.d("qqq", "Enter here time: " + time);
                            time++;
                            tcs.setResult(false);
                        } else {
                            Log.d("qqq", "Now enter to else");
                            if (!StringUtil.isNullOrEmpty(entity.getIdFs())) {
                                if (entity.getIdFs().equals(savedEntity.getIdFs())) {
                                    tcs.setResult(false);
                                } else {
                                    tcs.setResult(true);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("qqq", "Entering addFailureListener");
                        tcs.setException(e);
                        tcs.setResult(false);
                    });
        }
        else
            tcs.setResult(false);

        return tcs.getTask();
    }


    //region ADD
    public Task<Boolean> add(TEntity entity) {
        return add(entity, null, null);
    }


    public Task<Boolean> add(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<Boolean> taskAdd = new TaskCompletionSource<>();

        DocumentReference document = collection.document();
        entity.setIdFs(document.getId());

        handlePictureUpload(entity, pictureFieldName, pictureUrlFieldName)
                .addOnCompleteListener(task -> {
                    document.set(entity)
                            .addOnSuccessListener(aVoid -> taskAdd.setResult(true))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error adding member: " + e.getMessage());
                                taskAdd.setResult(false);
                            });
                });

        return taskAdd.getTask();
    }
    //endregion

    //region UPDATE
    public Task<Boolean> update(TEntity entity) {
        return update(entity, null, null);
    }

    public Task<Boolean> update(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<Boolean> taskUpdate = new TaskCompletionSource<>();

        DocumentReference document = collection.document(entity.getIdFs());

        handlePictureUpload(entity, pictureFieldName, pictureUrlFieldName)
                .addOnCompleteListener(task -> {
                    document.set(entity)
                            .addOnSuccessListener(aVoid -> taskUpdate.setResult(true))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating member: " + e.getMessage());
                                taskUpdate.setResult(false);
                            });
                });

        return taskUpdate.getTask();
    }
    //endregion

    //region DELETE
    public Task<Boolean> delete(TEntity entity) {
        return delete(entity, null, null);
    }

    public Task<Boolean> delete(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<Boolean> taskDelete = new TaskCompletionSource<>();

        DocumentReference document = collection.document(entity.getIdFs());
        document.delete()
                .addOnSuccessListener(aVoid -> {
                    if (!StringUtil.isNullOrEmpty(pictureFieldName) && !StringUtil.isNullOrEmpty(pictureUrlFieldName)) {
                        handlePictureDelete(entity, pictureFieldName, pictureUrlFieldName)
                                .addOnCompleteListener(task -> taskDelete.setResult(task.getResult()));
                    } else {
                        taskDelete.setResult(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting member: " + e.getMessage());
                    taskDelete.setResult(false);
                });

        return taskDelete.getTask();
    }
    //endregion

    //region GET
    public Task<TEntity> get(Query query){
        return get(query, null, null);
    }

    public Task<TEntity> get(Query query, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<TEntity> tcs = new TaskCompletionSource<>();

        query.limit(1).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            TEntity entity = document.toObject(getEntityType());

                            if (entity != null && !StringUtil.isNullOrEmpty(pictureFieldName) && !StringUtil.isNullOrEmpty(pictureUrlFieldName)) {
                                handlePictureDownload(entity, pictureFieldName, pictureUrlFieldName)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    tcs.setResult(entity);
                                                } else {
                                                    tcs.setException(task.getException());
                                                }
                                            }
                                        });
                            } else {
                                tcs.setResult(entity);
                            }
                        } else {
                            tcs.setResult(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tcs.setException(e);
                    }
                });

        return tcs.getTask();
    }

    public Task<TEntity> get(String idFs) {
        return get(idFs, null, null);
    }

    public Task<TEntity> get(String idFs, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<TEntity> taskMember = new TaskCompletionSource<>();

        collection.document(idFs).get()
                .addOnSuccessListener(documentSnapshot -> {
                    TEntity member = documentSnapshot.toObject(getEntityType());
                    if (member != null) {
                        handlePictureDownload(member, pictureFieldName, pictureUrlFieldName)
                                .addOnCompleteListener(task -> taskMember.setResult(member));
                    } else {
                        taskMember.setResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting member: " + e.getMessage());
                    taskMember.setResult(null);
                });

        return taskMember.getTask();
    }
    //endregion

    //region GET ALL
    private MutableLiveData<TCollection> collectionLiveData = new MutableLiveData<>();

    public LiveData<TCollection> getAll() {
        return getAll(null);
    }

    public LiveData<TCollection> getAll(Query query){
        return getAll(null, null, query);
    }

    public LiveData<TCollection> getAll(String pictureFieldName, String pictureUrlFieldName, Query query) {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        if (query == null)
            query = collection;

        listenerRegistration = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Error getting all members: " + e.getMessage());
                collectionLiveData.postValue(null);
                return;
            }

            try {
                TCollection tCollection = typeCollection.getDeclaredConstructor().newInstance();

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    List<Task<Void>> pictureTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TEntity entity = document.toObject(getEntityType());
                        if (entity != null) {
                            tCollection.add(entity);
                            if (!StringUtil.isNullOrEmpty(pictureFieldName) && !StringUtil.isNullOrEmpty(pictureUrlFieldName)) {
                                Task<Void> pictureTask = handlePictureDownload(entity, pictureFieldName, pictureUrlFieldName);
                                pictureTasks.add(pictureTask);
                            }
                        }
                    }

                    Tasks.whenAll(pictureTasks)
                            .addOnCompleteListener(task -> {
                                collectionLiveData.postValue(tCollection);
                            });
                } else {
                    collectionLiveData.postValue(tCollection);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error processing documents: " + ex.getMessage());
                collectionLiveData.postValue(null);
            }
        });

        return collectionLiveData;
    }

    // Method to stop listening for updates
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    /*
    private MutableLiveData<TCollection> sharedCollectionLiveData = new MutableLiveData<>();
    //private Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Handler timeoutHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            // This method needs to be implemented, but we won't use it for our timeout
        }
    };
    private static final long TIMEOUT_MS = 30000; // 30 seconds timeout

    public LiveData<TCollection> getAll(String pictureFieldName, String pictureUrlFieldName, Query query) {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        if (query == null) {
            query = collection;
        }

        final Query finalQuery = query;

        Runnable timeoutRunnable = () -> {
            Log.e(TAG, "Query timed out");
            stopListening();
            sharedCollectionLiveData.postValue(null);
        };

        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);

        listenerRegistration = finalQuery.addSnapshotListener((queryDocumentSnapshots, e) -> {
            timeoutHandler.removeCallbacks(timeoutRunnable);

            if (e != null) {
                Log.e(TAG, "Error getting all members: " + e.getMessage());
                sharedCollectionLiveData.postValue(null);
                return;
            }

            try {
                TCollection tCollection = typeCollection.getDeclaredConstructor().newInstance();

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    List<Task<Void>> pictureTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TEntity entity = document.toObject(getEntityType());
                        if (entity != null) {
                            tCollection.add(entity);
                            if (!StringUtil.isNullOrEmpty(pictureFieldName) && !StringUtil.isNullOrEmpty(pictureUrlFieldName)) {
                                Task<Void> pictureTask = handlePictureDownload(entity, pictureFieldName, pictureUrlFieldName);
                                pictureTasks.add(pictureTask);
                            }
                        }
                    }

                    if (!pictureTasks.isEmpty()) {
                        Tasks.whenAll(pictureTasks)
                                .addOnCompleteListener(task -> {
                                    sharedCollectionLiveData.postValue(tCollection);
                                });
                    } else {
                        sharedCollectionLiveData.postValue(tCollection);
                    }
                } else {
                    sharedCollectionLiveData.postValue(tCollection);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error processing documents: " + ex.getMessage());
                sharedCollectionLiveData.postValue(null);
            }
        });

        return sharedCollectionLiveData;
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        timeoutHandler.removeCallbacksAndMessages(null);
    }
    */

    /*
    public void cleanup() {
        stopListening();
        timeoutHandler.removeCallbacksAndMessages(null);
    }
    */
    //endregion

    //region HANDLE PICTURE (Upload, Download, Delete)
    private Task<Void> handlePictureUpload(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (!StringUtil.isNullOrEmpty(pictureFieldName) && !StringUtil.isNullOrEmpty(pictureUrlFieldName)) {
            try {
                String picture = (String) getFieldValue(entity, pictureFieldName);
                if (!StringUtil.isNullOrEmpty(picture)) {
                    FirebaseImageStorage.saveToStorage(picture, entity.getIdFs(), "images/" + getCollectionName())
                            .addOnSuccessListener(photoLocation -> {
                                try {
                                    setFieldValue(entity, pictureUrlFieldName, photoLocation);
                                    tcs.setResult(null);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error setting picture URL: " + e.getMessage());
                                    tcs.setResult(null);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error uploading picture: " + e.getMessage());
                                tcs.setResult(null);
                            });
                } else {
                    tcs.setResult(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling picture upload: " + e.getMessage());
                tcs.setResult(null);
            }
        } else {
            tcs.setResult(null);
        }

        return tcs.getTask();
    }

    private Task<Boolean> handlePictureDelete(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        try {
            String url = (String) getFieldValue(entity, pictureUrlFieldName);
            if (!StringUtil.isNullOrEmpty(url)) {
                FirebaseImageStorage.deleteFromStorage(entity.getIdFs(), "images/" + getCollectionName())
                        .addOnCompleteListener(task -> tcs.setResult(task.getResult()));
            } else {
                tcs.setResult(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling picture delete: " + e.getMessage());
            tcs.setResult(false);
        }

        return tcs.getTask();
    }

    protected Task<Void> handlePictureDownload(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (!StringUtil.isNullOrEmpty(pictureFieldName) && !StringUtil.isNullOrEmpty(pictureUrlFieldName)) {
            try {
                String url = (String) getFieldValue(entity, pictureUrlFieldName);
                if (!StringUtil.isNullOrEmpty(url)) {
                    FirebaseImageStorage.loadFromStorage(entity.getIdFs(), "images/" + getCollectionName())
                            .addOnSuccessListener(picture -> {
                                try {
                                    setFieldValue(entity, pictureFieldName, picture);
                                    tcs.setResult(null);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error setting picture: " + e.getMessage());
                                    tcs.setResult(null);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error downloading picture: " + e.getMessage());
                                tcs.setResult(null);
                            });
                } else {
                    tcs.setResult(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling picture download: " + e.getMessage());
                tcs.setResult(null);
            }
        } else {
            tcs.setResult(null);
        }

        return tcs.getTask();
    }
    //endregion

    //region REFLECTION
    private Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private void setFieldValue(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    //endregion
}