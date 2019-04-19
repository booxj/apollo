package com.booxj.opensource.mine.apollo.exception;

import org.springframework.http.HttpStatus;


public class NotFoundException extends AbstractApolloHttpException {

    public NotFoundException(String str) {
        super(str);
        setHttpStatus(HttpStatus.NOT_FOUND);
    }
}
