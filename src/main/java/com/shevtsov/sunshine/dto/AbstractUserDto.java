package com.shevtsov.sunshine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class AbstractUserDto extends AbstractDto {
    protected String roleName;

    public AbstractUserDto(Long id) {
        this.id = id;
    }
}
