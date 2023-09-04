package com.antares.common.model.enums;

import lombok.Getter;

@Getter
public enum DeleteEnum {
    Y(1),
    N(0);

    public final int isDelete;

    DeleteEnum(int isDelete){
            this.isDelete = isDelete;
        }

}
