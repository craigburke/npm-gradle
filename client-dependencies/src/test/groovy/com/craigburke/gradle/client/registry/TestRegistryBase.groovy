package com.craigburke.gradle.client.registry

import com.craigburke.clientdependencies.api.dependency.Dependency
import com.craigburke.clientdependencies.api.dependency.SimpleDependency
import com.craigburke.clientdependencies.api.registry.core.AbstractRegistry
import com.craigburke.clientdependencies.api.registry.core.Resolver

/**
 *
 * Minimal AbstractRegistry implementation for testing
 *
 * @author Craig Burke
 */
class TestRegistryBase extends AbstractRegistry {

    TestRegistryBase(String name, String url, List<Class<Resolver>> resolvers) {
        super(name, url, ['foo.json'], resolvers)
    }

    boolean loadSourceFromGlobalCache(Dependency dependency) { false }
    List<SimpleDependency> getChildDependencies(Dependency dependency) { null }
    Map loadInfoFromGlobalCache(Dependency dependency) { null }
    Map loadInfoFromRegistry(Dependency dependency) { null }
}
