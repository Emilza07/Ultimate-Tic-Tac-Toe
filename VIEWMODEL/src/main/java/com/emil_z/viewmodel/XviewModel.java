package com.emil_z.viewmodel;

import android.app.Application;

import com.emil_z.repository.BASE.BaseRepository;
import com.emil_z.viewmodel.BASE.BaseViewModel;

public class XviewModel extends BaseViewModel {
    @Override
    protected BaseRepository createRepository(Application application) {
        return null;
    }

    public XviewModel(Class tEntity, Class tCollection, Application application) {
        super(tEntity, tCollection, application);
    }
}