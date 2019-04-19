package com.booxj.opensource.mine.apollo.sample.metaservice.service;

import com.booxj.opensource.mine.apollo.core.ServiceNameConstants;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 封装了EurekaClient的相关方法
 */
@Service
public class DiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryService.class);

    private final EurekaClient eurekaClient;

    public DiscoveryService(@Qualifier("eurekaClient") final EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    public List<InstanceInfo> getConfigServiceInstances() {
        Application application = eurekaClient.getApplication(ServiceNameConstants.APOLLO_CONFIGSERVICE);
        if (application == null) {
            logger.info("Apollo.EurekaDiscovery.NotFound", ServiceNameConstants.APOLLO_CONFIGSERVICE);
        }
        return application != null ? application.getInstances() : Collections.emptyList();
    }

    public List<InstanceInfo> getMetaServiceInstances() {
        Application application = eurekaClient.getApplication(ServiceNameConstants.APOLLO_METASERVICE);
        if (application == null) {
            logger.info("Apollo.EurekaDiscovery.NotFound", ServiceNameConstants.APOLLO_METASERVICE);
        }
        return application != null ? application.getInstances() : Collections.emptyList();
    }

    public List<InstanceInfo> getAdminServiceInstances() {
        Application application = eurekaClient.getApplication(ServiceNameConstants.APOLLO_ADMINSERVICE);
        if (application == null) {
            logger.info("Apollo.EurekaDiscovery.NotFound", ServiceNameConstants.APOLLO_ADMINSERVICE);
        }
        return application != null ? application.getInstances() : Collections.emptyList();
    }
}
