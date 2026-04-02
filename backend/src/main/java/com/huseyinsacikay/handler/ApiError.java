package com.huseyinsacikay.handler;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter
public class ApiError<E> {
    private Integer status;
    private InternalException<E> exception; // Java'nın kendi Exception sınıfıyla karışmaması için adını değiştirdik
}

@Getter @Setter
class InternalException<E> {
    private String hostName;
    private String path;
    private OffsetDateTime createTime;
    private E message;
}