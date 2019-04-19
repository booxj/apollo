package com.booxj.opensource.mine.apollo.sample.metaservice.controller;

import com.booxj.opensource.mine.apollo.core.dto.ServiceDTO;
import com.booxj.opensource.mine.apollo.sample.metaservice.service.DiscoveryService;
import com.netflix.appinfo.InstanceInfo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final DiscoveryService discoveryService;

    /**
     * 将InstanceInfo转化为ServiceDTO
     * 获取服务集群
     */
    private static Function<InstanceInfo, ServiceDTO> instanceInfoToServiceDTOFunc = instance -> {
        ServiceDTO service = new ServiceDTO();
        service.setAppName(instance.getAppName());
        service.setInstanceId(instance.getInstanceId());
        service.setHomepageUrl(instance.getHomePageUrl());
        return service;
    };

    public ServiceController(final DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }


    @RequestMapping("/meta")
    public List<ServiceDTO> getMetaService() {
        List<InstanceInfo> instances = discoveryService.getMetaServiceInstances();
        List<ServiceDTO> result = instances.stream().map(instanceInfoToServiceDTOFunc).collect(Collectors.toList());
        return result;
    }

    @RequestMapping("/config")
    public List<ServiceDTO> getConfigService() {
        List<InstanceInfo> instances = discoveryService.getConfigServiceInstances();
        List<ServiceDTO> result = instances.stream().map(instanceInfoToServiceDTOFunc).collect(Collectors.toList());
        return result;
    }

    @RequestMapping("/admin")
    public List<ServiceDTO> getAdminService() {
        List<InstanceInfo> instances = discoveryService.getAdminServiceInstances();
        List<ServiceDTO> result = instances.stream().map(instanceInfoToServiceDTOFunc).collect(Collectors.toList());
        return result;
    }
}
