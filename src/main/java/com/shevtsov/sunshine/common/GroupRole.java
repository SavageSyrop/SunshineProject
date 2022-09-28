package com.shevtsov.sunshine.common;

/**
 * Определяет уровень доступа и прав в группе
 * OWNER - всегда существует лишь в единственном экземпляре
 * AWAITING_CHECK - роль, существующая лишь в группах с параметром openToJoin=false. Означает запрос выдачу роли SUBSCRIBER,
 * в остальном имеет такой же доступ как любой пользователь, не являющийся участником группы
 */

public enum GroupRole {
    OWNER,
    ADMIN,
    SUBSCRIBER,
    AWAITING_CHECK
}
