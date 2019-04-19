package com.booxj.opensource.mine.apollo.sample.configservice.controller;


import com.booxj.opensource.mine.apollo.sample.configservice.component.RetryableRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RetryController {

    @Autowired
    private RetryableRestTemplate restTemplate;

    @GetMapping("retry")
    public String retry() {
        return restTemplate.get("config", "hello/{name}", String.class, "booxj");
    }

    @GetMapping("hello/{name}")
    public String hello(@PathVariable String name) {
        return "hello , " + name;
    }
}
