package com.emil_z.repository;

import com.google.firebase.firestore.Query;

import com.emil_z.model.BASE.BaseEntity;
import com.emil_z.repository.BASE.BaseRepository;

public class Xrepository extends BaseRepository {
    @Override
    protected Query getQueryForExist(BaseEntity entity) {
        return null;
    }
}