package com.craigburke.clientdependencies.api.registry.core

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 *
 * Exception for a circular dependency
 *
 * @author Craig Burke
 */
@InheritConstructors
@CompileStatic
class DependencyResolveException extends RuntimeException {
}
