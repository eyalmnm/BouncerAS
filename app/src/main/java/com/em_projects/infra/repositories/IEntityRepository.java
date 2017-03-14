package com.em_projects.infra.repositories;

import com.em_projects.infra.model.Entity;

import java.util.List;

public interface IEntityRepository<T extends Entity<UID>, UID> {
    /**
     * @return returns the full list of UIDs of the existing entities.
     */
    public abstract List<UID> getAllUIDs();

    /**
     * @return returns loaded entity by uid.
     */
    public abstract T getByUID(UID uid);

    /**
     * @return returns loaded entities (note: this method might be time consuming)
     */
    public abstract List<T> getAll();

    /**
     * @return returns entities by list of UIDs.
     */
    public abstract List<T> getAll(List<UID> uids);

    /**
     * Deletes the entity with the given UID.
     *
     * @param uid unique id.
     * @return true if success false otherwise.
     */
    public abstract boolean delete(UID uid);

    /**
     * Saves the list of entities
     *
     * @param entities
     */
    public abstract void save(List<T> entities);
}
