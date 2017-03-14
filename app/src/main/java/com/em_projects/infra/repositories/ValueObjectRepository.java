package com.em_projects.infra.repositories;

import com.em_projects.infra.model.ValueObject;

import java.util.Vector;

public abstract class ValueObjectRepository<T extends ValueObject> {
    public abstract Vector<T> getAll();
}
