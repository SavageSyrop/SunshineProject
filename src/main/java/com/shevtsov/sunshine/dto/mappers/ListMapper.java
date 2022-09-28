package com.shevtsov.sunshine.dto.mappers;

import com.shevtsov.sunshine.dto.AbstractDto;
import com.shevtsov.sunshine.dao.entities.AbstractEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class ListMapper<D extends AbstractDto, T extends AbstractEntity> {
    public abstract D toDto(T entity);

    public T toEntity(D dto) {
        return null;
    }

    public List<D> toListDto(List<T> entities) {
        List<D> dtos = new ArrayList<>();
        for (T entity : entities) {
            dtos.add(toDto(entity));
        }
        return dtos;
    }
}
