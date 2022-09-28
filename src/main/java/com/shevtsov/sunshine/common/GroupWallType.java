package com.shevtsov.sunshine.common;

/**
 * Определяет уровень доступа к стене группы
 * OFFERED_POSTS - тип доступа, в котором посты проходят предварительную модерацию перед публикацией. Модерацию производят участники
 * с ролями ADMIN, OWNER
 */

public enum GroupWallType {
    ALL,
    OFFERED_POSTS,
    SUBSCRIBERS,
    ADMINS,
    OWNER
}
