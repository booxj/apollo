##### 配置更新链路

```sequence
ConfigRepository->DefaultConfig(RepositoryChangeListener):注册(创建DefaultConfig时完成注册)
DefaultConfig(RepositoryChangeListener)->AutoUpdateConfigChangeListener(ConfigChangeListener):注册(所有Config的子类都被加入监听)
ConfigRepository-->DefaultConfig(RepositoryChangeListener):长连接和轮训两种方式查询配置是否发生变化\n如果发现配置变化，发送监听事件\n触发DefaultConfig的onRepositoryChange方法
DefaultConfig(RepositoryChangeListener)-->AutoUpdateConfigChangeListener(ConfigChangeListener):在onRepositoryChange方法中封装参数，触发\nAutoUpdateConfigChangeListener的onChange方法
 Note right of AutoUpdateConfigChangeListener(ConfigChangeListener):通过反射修改配置
```







