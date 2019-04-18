package com.booxj.opensource.mine.apollo.sample.property.internals;

import java.util.Properties;

public interface RepositoryChangeListener {

    void onRepositoryChange(String namespace, Properties newProperties);

}
