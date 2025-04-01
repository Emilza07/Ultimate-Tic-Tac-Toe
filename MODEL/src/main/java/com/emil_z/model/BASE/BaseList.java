package com.emil_z.model.BASE;

import java.io.Serializable;
import java.util.ArrayList;


public abstract class BaseList<TEntity ,TCollection> extends ArrayList<TEntity> implements Serializable {
}

/*
public abstract class BaseList<TEntity ,TCollection> extends ArrayList<TEntity> {

    protected boolean            sortList;
    protected Class<TEntity>     typeEntity;
    protected Class<TCollection> typeCollection;

    public BaseList(Class<TEntity> tEntity, Class<TCollection> tCollection) {
        this(true, tEntity, tCollection);
    }

    public BaseList(boolean sortList, Class<TEntity> tEntity, Class<TCollection> tCollection) {
        this.sortList = sortList;
        typeEntity = tEntity;
        typeCollection = tCollection;
    }

    public boolean add(TEntity entity) {
        if (!exist(entity)) {

            ((BaseEntity) entity).entityStatus = EntityStatus.ADDED;
            super.add(entity);

            if (sortList) sort();

            return true;
        } else
            return false;
    }

    public boolean modify(TEntity oldEntity, TEntity newEntity) {

        if (!oldEntity.equals(newEntity)) {
            if (!exist(newEntity)) {

                if (((BaseEntity) oldEntity).idFs != null && ((BaseEntity) oldEntity).idFs.isEmpty())
                    ((BaseEntity) newEntity).entityStatus = EntityStatus.ADDED;
                else
                    ((BaseEntity) newEntity).entityStatus = EntityStatus.MODIFIED;

                this.set(indexOf(oldEntity), newEntity);

                if (sortList) sort();

                return true;
            }

            return false;
        } else
            return false;
    }

    public boolean delete(TEntity entity) {
        if (((BaseEntity) entity).idFs == null && ((BaseEntity) entity).idFs.isEmpty())
            remove(entity);
        else {
            ((BaseEntity) entity).entityStatus = EntityStatus.DELETED;
        }

        return true;
    }

    public ArrayList<TEntity> insertList;
    public ArrayList<TEntity> updateList;
    public ArrayList<TEntity> deleteList;

    protected void genereteUpdateLists() {
        insertList = (ArrayList<TEntity>) this.stream()
                .filter(item -> ((BaseEntity) item).entityStatus == EntityStatus.ADDED)
                .collect(Collectors.toList());

        updateList = (ArrayList<TEntity>) this.stream()
                .filter(item -> ((BaseEntity) item).entityStatus == EntityStatus.MODIFIED)
                .collect(Collectors.toList());

        deleteList = (ArrayList<TEntity>) this.stream()
                .filter(item -> ((BaseEntity) item).entityStatus == EntityStatus.DELETED)
                .collect(Collectors.toList());
    }

    public TCollection undeletedList() {
        return (TCollection) this.stream()
                .filter(item -> ((BaseEntity) item).entityStatus != EntityStatus.DELETED)
                .collect(Collectors.toList());
    }

    protected boolean isUpdateOK;

    public boolean baseSave() {
        removeIf(item -> ((BaseEntity) item).entityStatus == EntityStatus.DELETED);
        stream().filter(item -> ((BaseEntity) item).entityStatus == EntityStatus.UNCHANGED);

        return isUpdateOK;
    }

    public boolean save() {
        return this.save(null, null, null);
    }

    public boolean save(String imageFied, String imageUrl, String path) {
        boolean isOk = false;

        genereteUpdateLists();

        if (insertList.size() > 0)
            for (TEntity c : insertList) {
                if (imageField == null) {
                    //isOk = (new BaseRepository<TEntity, TCollection>(typeEntity, typeCollection)).add(c);
                    //else
                    //    isOk = (new DbTable<TEntity, TCollection>()).add(c, imageFied, imageUrl, path);
                }

//        if (UpdateList.Count > 0)
//            foreach (TEntity c in UpdateList)
//        if (imageFied == null)
//            isOk = await FireStoreDbTable<TEntity, TCollection>.Update(c);
//                    else
//        isOk = await FireStoreDbTable<TEntity, TCollection>.Update(c, imageFied, imageUrl, path);
//
//        if (DeleteList.Count > 0)
//            foreach (TEntity c in DeleteList)
//        if (imageFied == null)
//            isOk = await FireStoreDbTable<TEntity, TCollection>.Delete(c);
//                    else
//        isOk = await FireStoreDbTable<TEntity, TCollection>.Delete(c, path);

                baseSave();

                return isOk;
            }

        return isOk;
    }


    public abstract boolean exist(TEntity entity);
    public abstract void sort();
}
*/