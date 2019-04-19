package com.booxj.opensource.mine.apollo.sample.configservice.component;

import com.booxj.opensource.mine.apollo.core.dto.ServiceDTO;
import com.google.common.collect.Lists;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import static org.springframework.http.HttpMethod.*;

/**
 *
 */
@Component
public class AddressLocator {

    // 定时器，定时更新所有服务器的集群地址
    private ScheduledExecutorService refreshServiceAddressService;

    // 缓存服务集群信息
    private Map<String, List<ServiceDTO>> cache = new ConcurrentHashMap<>();

    private RestTemplate restTemplate;

    public AddressLocator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ServiceDTO> getServiceList(String serverName) {
        // uri: {host:ip/services/${serverName}}
        // 可做成可配置文件
        String host = "http://localhost:8080";
        StringBuilder uri = new StringBuilder();
        uri.append(host).append("/services/").append(serverName);

        ParameterizedTypeReference<List<ServiceDTO>> responseType = new ParameterizedTypeReference<List<ServiceDTO>>() {
        };

        List<ServiceDTO> services = restTemplate.exchange(uri.toString(), GET, null, responseType).getBody();

        //List<ServiceDTO> services = cache.get(serverName);
        if (CollectionUtils.isEmpty(services)) {
            return Collections.emptyList();
        }

        List<ServiceDTO> randomConfigServices = Lists.newArrayList(services);
        Collections.shuffle(randomConfigServices);
        return randomConfigServices;
    }

}
