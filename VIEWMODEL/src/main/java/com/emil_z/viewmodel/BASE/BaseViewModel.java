package com.emil_z.viewmodel.BASE;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.emil_z.helper.StringUtil;
import com.emil_z.model.BASE.BaseEntity;
import com.emil_z.model.BASE.BaseList;
import com.emil_z.repository.BASE.BaseRepository;

public abstract class BaseViewModel<TEntity extends BaseEntity, TCollection extends BaseList<TEntity, TCollection>> extends AndroidViewModel {
    private   final String TAG = "BaseViewModel";
    private   Class<TEntity>                        typeEntity;
    private   Class<TCollection>                    typeCollection;
    protected MutableLiveData<Boolean>              lvSuccess;
    protected MutableLiveData<Boolean>              lvExist;
    protected MutableLiveData<TEntity>              lvEntity;
    protected /*Mutable*/LiveData<TCollection>      lvCollection;
    protected BaseRepository<TEntity, TCollection>  repository;
    private   MutableLiveData<Boolean>              isLoading; // For loading indicator

    protected abstract BaseRepository<TEntity, TCollection> createRepository(Application application);

    public BaseViewModel(Class<TEntity> tEntity, Class<TCollection> tCollection, Application application) {
        super(application);
        typeEntity = tEntity;
        typeCollection = tCollection;

        lvSuccess    = new MutableLiveData<>();
        lvExist      = new MutableLiveData<>();
        lvCollection = new MutableLiveData<>();
        lvEntity     = new MutableLiveData<>();
        isLoading    = new MutableLiveData<>(false);

        //repository = new BaseRepository<>(typeEntity, typeCollection, application);
        repository = createRepository(application);
    }

    public LiveData<Boolean> getSuccess(){
        return lvSuccess;
    }

    public LiveData<TEntity> getEntity(){
        return lvEntity;
    }

    public LiveData<Boolean> getIsLoading(){
        return isLoading;
    }

    //region INITIALIZE
    public Class<TEntity> getEntityType() {
        return typeEntity;
    }

    public String getEntityName() {
        return typeEntity.getSimpleName();
    }

    public Class<TCollection> getCollectionType() {
        return typeCollection;
    }

    public String getCollectionName() {
        return typeCollection.getSimpleName();
    }

    public LiveData<Boolean> getLiveDataSuccess() {
        return lvSuccess;
    }

    public LiveData<TEntity> getLiveDataEntity() {
        return lvEntity;
    }

    public LiveData<TCollection> getLiveDataCollection() {
        return lvCollection;
    }
    //endregion

    public void exist (TEntity entity){
        repository.exist(entity)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        lvSuccess.setValue(aBoolean);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        lvSuccess.setValue(false);
                    }
                });
    }

    //region SAVE
    public void save(TEntity entity) {
        save(entity, null, null);
    }

    public void save(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        repository.exist(entity)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (!aBoolean) {
                            if (StringUtil.isNullOrEmpty(entity.getIdFs()))
                                add(entity, pictureFieldName, pictureUrlFieldName);
                            else
                                update(entity, pictureFieldName, pictureUrlFieldName);
                        }
                        else {
                            lvSuccess.setValue(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Exist fail");
                    }
                });
    }
    //endregion

    //region ADD
    public void add(TEntity entity) {
        add(entity, null, null);
    }

    public void add(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        repository.add(entity, pictureFieldName, pictureUrlFieldName)
                .addOnSuccessListener(aBoolean -> {
                    lvSuccess.setValue(aBoolean);
                })
                .addOnFailureListener(e -> {
                    lvSuccess.setValue(false);
                });
    }
    //endregion

    //region UPDATE
    public void update(TEntity entity) {
        update(entity, null, null);
    }

    public void update(TEntity member, String pictureFieldName, String pictureUrlFieldName) {
        repository.update(member, pictureFieldName, pictureUrlFieldName)
                .addOnSuccessListener(aBoolean -> {
                    lvSuccess.setValue(aBoolean);
                })
                .addOnFailureListener(e -> {
                    lvSuccess.setValue(false);
                });
    }
    //endregion

    //region DELETE
    public void delete(TEntity entity) {
        delete(entity, null, null);
    }

    public void delete(TEntity entity, String pictureFieldName, String pictureUrlFieldName) {
        repository.delete(entity, pictureFieldName, pictureUrlFieldName)
                .addOnSuccessListener(aBoolean -> {
                    lvSuccess.setValue(aBoolean);
                })
                .addOnFailureListener(e -> {
                    lvSuccess.setValue(false);
                });
    }
    //endregion

    //region GET
    public void get(String idFs) {
        get(idFs, null, null);
    }

    public void get(String idFs, String pictureFieldName, String pictureUrlFieldName) {
        repository.get(idFs, pictureFieldName, pictureUrlFieldName)
                .addOnSuccessListener(member -> {
                    lvEntity.setValue(member);
                })
                .addOnFailureListener(e -> {
                    lvEntity.setValue(null);
                });
    }

    public void get(Query query){
        get(query, null, null);
    }

    public void get(Query query, String pictureFieldName, String pictureUrlFieldName){
        repository.get(query, pictureFieldName, pictureUrlFieldName)
                .addOnSuccessListener(entity -> {lvEntity.setValue(entity);})
                .addOnFailureListener(e -> {lvEntity.setValue(null);});
    }
    //endregion

    //region GET ALL
    public void getAll() {
        getAll(null);
    }

    public void getAll(Query query) {
        getAll(null, null, query);
    }

    public void getAll(String pictureFieldName, String pictureUrlFieldName, Query query) {
        lvCollection = repository.getAll(pictureFieldName, pictureUrlFieldName, query);
    }

    public void stopListening(){
        repository.stopListening();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }
    //endregion
}