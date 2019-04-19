package com.booxj.opensource.mine.apollo.core.dto;

import java.util.Map;

public class ApolloConfig {

    //命名空间
    private String namespaceName;

    //配置
    private Map<String,String> configurations;

    //发布版本号
    private String releaseKey;


    public ApolloConfig(String namespaceName, Map<String, String> configurations, String releaseKey) {
        this.namespaceName = namespaceName;
        this.configurations = configurations;
        this.releaseKey = releaseKey;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, String> configurations) {
        this.configurations = configurations;
    }

    public String getReleaseKey() {
        return releaseKey;
    }

    public void setReleaseKey(String releaseKey) {
        this.releaseKey = releaseKey;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApolloConfig{");
        sb.append("namespaceName='").append(namespaceName).append('\'');
        sb.append(",configurations='").append(configurations).append('\'');
        sb.append(",releaseKey='").append(releaseKey).append('\'');
        sb.append("}");
        return sb.toString();
    }
}
