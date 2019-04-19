package com.booxj.opensource.mine.apollo.sample.configservice.component;

import com.booxj.opensource.mine.apollo.core.dto.ServiceDTO;
import com.booxj.opensource.mine.apollo.exception.ServiceException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 基于封装RestTemplate实现轮训和超时重试功能
 */
@Component
public class RetryableRestTemplate {

    private Logger logger = LoggerFactory.getLogger(RetryableRestTemplate.class);

    private final RestTemplate restTemplate;
    private final AddressLocator addressLocator;

    private UriTemplateHandler uriTemplateHandler = new DefaultUriBuilderFactory();

    public RetryableRestTemplate(RestTemplate restTemplate, AddressLocator addressLocator) {
        this.restTemplate = restTemplate;
        this.addressLocator = addressLocator;
    }


    public <T> T get(String serverName, String path, Class<T> responseType, Object... urlVariables)
            throws RestClientException {
        return execute(HttpMethod.GET, serverName, path, responseType, responseType, urlVariables);
    }


    private <T> T execute(HttpMethod method, String serverName, String path, Object request, Class<T> responseType,
                          Object... uriVariables) {

        String uri = uriTemplateHandler.expand(path, uriVariables).getPath();

        List<ServiceDTO> services = addressLocator.getServiceList(serverName);

        for (ServiceDTO serviceDTO : services) {
            try {

                T result = doExecute(method, serviceDTO, uri, request, responseType, uriVariables);
                return result;
            } catch (Throwable t) {
                logger.error("Http request failed, uri: {}, method: {}", serviceDTO.getHomepageUrl() + "/" + uri, method);
                if (canRetry(t, method)) {
                    logger.info("Http request begin retry, uri: {}, method: {}", serviceDTO.getHomepageUrl() + serviceDTO.getHomepageUrl() + "/" + uri, method);
                } else {
                    //biz exception rethrow
                    throw t;
                }
            }
        }

        //all server down
        ServiceException e = new ServiceException(
                String.format("All %s servers are unresponsive. servers: %s", serverName, services));
        throw e;
    }

    private <T> T doExecute(HttpMethod method, ServiceDTO service, String path, Object request,
                            Class<T> responseType,
                            Object... uriVariables) {
        T result = null;
        switch (method) {
            case GET:
                result = restTemplate.getForObject(parseHost(service) + path, responseType, uriVariables);
                break;
            case POST:
                result = restTemplate.postForEntity(parseHost(service) + path, request, responseType, uriVariables).getBody();
                break;
            case PUT:
                restTemplate.put(parseHost(service) + path, request, uriVariables);
                break;
            case DELETE:
                restTemplate.delete(parseHost(service) + path, uriVariables);
                break;
            default:
                throw new UnsupportedOperationException(String.format("unsupported http method(method=%s)", method));
        }
        return result;
    }

    private String parseHost(ServiceDTO serviceAddress) {
        return serviceAddress.getHomepageUrl() + "/";
    }

    //post,delete,put请求在admin server处理超时情况下不重试
    private boolean canRetry(Throwable e, HttpMethod method) {
        Throwable nestedException = e.getCause();
        if (method == HttpMethod.GET) {
            return nestedException instanceof SocketTimeoutException
                    || nestedException instanceof HttpHostConnectException
                    || nestedException instanceof ConnectTimeoutException;
        } else {
            return nestedException instanceof HttpHostConnectException
                    || nestedException instanceof ConnectTimeoutException;
        }
    }
}
