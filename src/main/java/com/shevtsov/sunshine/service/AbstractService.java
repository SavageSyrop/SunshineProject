package com.shevtsov.sunshine.service;

import com.shevtsov.sunshine.dao.entities.AbstractEntity;

import java.util.List;

public interface AbstractService<T extends AbstractEntity> {
    T getById(Long id);

    List<T> getAll();

    void deleteById(Long id);

    T create(T entity);

    void update(T entity);
}


