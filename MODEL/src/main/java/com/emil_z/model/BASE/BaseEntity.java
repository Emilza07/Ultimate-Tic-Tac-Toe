package com.emil_z.model.BASE;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public abstract class BaseEntity implements Serializable {
    @Exclude
    protected int    id;
    protected String idFs;
    @Exclude
    protected EntityStatus entityStatus;

    public BaseEntity() {
        id           = 0;
        idFs         = "";
        entityStatus = EntityStatus.UNCHANGED;
    }

       // GRADLE: implementation ("com.google.firebase:firebase-firestore:25.0.0")
    @Exclude
    public int getId() {
        return id;
    }

    @Exclude
    public void setId(int id) {
        this.id = id;
    }

    public String getIdFs() {
        return idFs;
    }

    public void setIdFs(String idFs) {
        this.idFs = idFs;
    }

    @Exclude
    public EntityStatus getEntityStatus() {
        return entityStatus;
    }
    @Exclude
    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id == that.id && idFs.equals(that.idFs) && entityStatus.equals(that.entityStatus);
    }
}