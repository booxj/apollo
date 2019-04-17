package com.ctrip.framework.apollo.portal.api;


import com.ctrip.framework.apollo.portal.component.RetryableRestTemplate;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Portal 所有远程调用API，统一继承RetryableRestTemplate
 */
public abstract class API {

    // 封装RestTemplate，并添加啊重试机制
    @Autowired
    protected RetryableRestTemplate restTemplate;

}
