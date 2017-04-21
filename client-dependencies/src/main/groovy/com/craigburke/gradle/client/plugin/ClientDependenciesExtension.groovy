/*
 * Copyright 2016 Craig Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.craigburke.gradle.client.plugin

import com.craigburke.clientdependencies.api.config.ClientDependenciesConfig
import com.craigburke.clientdependencies.api.registry.core.Registry
import org.gradle.api.Project
import org.gradle.api.logging.Logging

/**
 *
 * Extension for client dependencies
 *
 * @author Craig Burke
 */
class ClientDependenciesExtension {

    Project project

    ClientDependenciesConfig config
    int threadPoolSize = 15

    ClientDependenciesExtension(Project project) {
        this.project = project
        this.config = new ClientDependenciesConfig(Logging.getLogger(ClientDependenciesExtension))
    }

    void setFileExtensions(Object... extensions) {
        config.setFileExtensions(extensions)
    }

    void fileExtensions(Object... extensions) {
        config.addFileExtensions(extensions)
    }

    List<String> getFileExtensions() {
        config.getFileExtensions()
    }

    void setReleaseFolders(Object... extensions) {
        config.setReleaseFolders(extensions)
    }

    void releaseFolders(Object... extensions) {
        config.addReleaseFolders(extensions)
    }

    List<String> getCopyIncludes() {
        config.getCopyIncludes()
    }

    void setCopyIncludes(Object... includes) {
        config.setCopyIncludes(includes)
    }

    void copyIncludes(Object... includes) {
        config.addCopyIncludes(includes)
    }

    List<String> getCopyExcludes() {
        config.getCopyExcludes()
    }

    void setCopyExcludes(Object... includes) {
        config.setCopyExcludes(includes)
    }

    void copyExcludes(Object... includes) {
        config.addCopyExcludes(includes)
    }

    List<String> getReleaseFolders() {
        config.getReleaseFolders()
    }

    void setInstallDir(Object installDir) {
        File realDir = null
        if ( installDir instanceof String) {
            realDir = new File(installDir)
        }
        else if ( installDir instanceof File) {
            realDir = installDir
        }
        config.installDir = realDir
    }

    File getInstallDir() {
        config.installDir
    }

    void setCacheDir(Object cacheDir) {
        File realDir = null
        if ( cacheDir instanceof String) {
            realDir = new File(cacheDir)
        }
        else if ( cacheDir instanceof File) {
            realDir = cacheDir
        }
        config.cacheDir = realDir
    }

    File getCacheDir() {
        config.getCacheDir
    }

    def methodMissing(String registryName, args) {
        config.methodMissing( registryName, args )
    }

    void registry(Map props = [:], String name) {
        config.registry( props, name )
    }

    Registry findRegistry(String name) {
        config.findRegistry( name )
    }

    Closure getCopyConfig() {
        config.copyConfig
    }
}
