package com.ctrip.framework.apollo.biz.utils;


import com.google.common.base.Joiner;

import com.ctrip.framework.apollo.core.ConfigConsts;

public class ReleaseMessageKeyGenerator {

  private static final Joiner STRING_JOINER = Joiner.on(ConfigConsts.CLUSTER_NAMESPACE_SEPARATOR);

  /**
   * 将appId cluster namespace 拼接
   * @param appId
   * @param cluster
   * @param namespace
   * @return
   */
  public static String generate(String appId, String cluster, String namespace) {
    return STRING_JOINER.join(appId, cluster, namespace);
  }
}
