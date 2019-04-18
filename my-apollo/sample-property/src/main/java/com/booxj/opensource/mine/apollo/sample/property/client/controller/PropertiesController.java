package com.booxj.opensource.mine.apollo.sample.property.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Company: 浙江核新同花顺网络信息股份有限公司
 * @ClassName: ApolloController.java
 * @Description: TODO
 * @Author: wangbo@myhexin.com
 * @CreateDate 2019/4/16 16:59
 * @version: 2.1.0
 */
@RestController
public class PropertiesController {

    private static Logger logger = LoggerFactory.getLogger(PropertiesController.class);

    @Value("${apollo.value}")
    private String value;

    @GetMapping("value")
    public String getValue() {
        logger.info("value=" + value);
        return value;
    }

}