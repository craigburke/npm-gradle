package com.craigburke.clientdependencies.api.registry.core

import com.craigburke.clientdependencies.api.dependency.Dependency
import com.craigburke.clientdependencies.api.dependency.Version
import groovy.transform.CompileStatic

/**
 *
 * Resolver interface
 *
 * @author Craig Burke
 */
@CompileStatic
interface Resolver {
    boolean canResolve(Dependency dependency)
    List<Version> getVersionList(Dependency dependency)
    void resolve(Dependency dependency)
    void afterInfoLoad(Dependency dependency)
}
