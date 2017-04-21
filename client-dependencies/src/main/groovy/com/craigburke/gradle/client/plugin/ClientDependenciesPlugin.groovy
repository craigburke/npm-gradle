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

import com.craigburke.clientdependencies.api.config.ClientDependenciesTasks
import com.craigburke.clientdependencies.api.registry.bower.BowerRegistry
import com.craigburke.clientdependencies.api.registry.core.AbstractRegistry
import com.craigburke.clientdependencies.api.registry.core.Registry
import com.craigburke.clientdependencies.api.registry.npm.NpmRegistry
import com.craigburke.clientdependencies.api.registry.npm.YarnRegistry
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logging
import org.gradle.internal.os.OperatingSystem

/**
 *
 * Main client dependencies plugin class
 *
 * @author Craig Burke
 */
class ClientDependenciesPlugin implements Plugin<Project> {

    static final String TASK_GROUP = 'Client Dependencies'
    static final String INSTALL_TASK = 'clientInstall'
    static final String CLEAN_TASK = 'clientClean'
    static final String REFRESH_TASK = 'clientRefresh'
    static final String REPORT_TASK = 'clientReport'

    static final String[] INSTALL_DEPENDENT_TASKS = ['run', 'bootRun', 'assetCompile',
        'integrationTest', 'karmaRun', 'karmaWatch']

    ClientDependenciesExtension config
    ClientDependenciesTasks tasks
    
    void apply(Project project) {
        config = project.extensions.create('clientDependencies', ClientDependenciesExtension, project)
        
        config.registries = [
            new NpmRegistry('npm', Logging.getLogger(ClientDependenciesPlugin)),
            new BowerRegistry('bower', Logging.getLogger(ClientDependenciesPlugin)),
            new YarnRegistry('yarn', Logging.getLogger(ClientDependenciesPlugin))
        ]
        
        project.task(CLEAN_TASK, group: TASK_GROUP,
                description: 'Clears the project cache and removes all files from the installDir') {
            doLast {
                tasks.clean()
            }
        }

        project.task(INSTALL_TASK, group: TASK_GROUP, description: 'Resolves and installs client dependencies') {
            mustRunAfter CLEAN_TASK
            onlyIf { !tasks.clientDependenciesAreInstalled() }
            doLast {
                tasks.install()
            }
        }

        project.task(REPORT_TASK, group: TASK_GROUP, description: 'Displays a dependency tree for the client dependencies') {
            doLast {
                tasks.report(project.logger)
            }
        }

        project.task(REFRESH_TASK, group: TASK_GROUP, dependsOn: [CLEAN_TASK, INSTALL_TASK],
                description: 'Clears project cache and re-installs all client dependencies')

        project.afterEvaluate {
            AbstractRegistry.threadPoolSize = config.threadPoolSize
            setDefaults(project)
            setTaskDependencies(project)
        }
    }
    void setDefaults(Project project) {
        if (config.installDir == null) {
            boolean usesGrails = project.extensions.findByName('grails')
            config.installDir = project.file(usesGrails ? 'grails-app/assets/vendor' : 'src/assets/vendor').absolutePath
        }

        if (config.cacheDir == null) {
            config.cacheDir = "${project.buildDir.path}/client-cache"
        }
        config.registries.each { Registry registry ->
            registry.offline = project.gradle.startParameter.isOffline()
            registry.localCacheDir = project.file("${config.cacheDir.absolutePath}/${registry.name}/")
            registry.globalCacheDir = registry.globalCacheDir ?: getDefaultGlobalCache(registry)
        }
        tasks.setDefaults()
    }

    static File getDefaultGlobalCache(Registry registry) {
        OperatingSystem os = OperatingSystem.current()
        String userHome = System.getProperty('user.home') as String

        String cachePath
        if (registry instanceof NpmRegistry) {
            cachePath = os.windows ? "${userHome}/AppData/npm-cache/" : "${userHome}/.npm/"
        }
        else {
            cachePath = os.windows ? "${userHome}/AppData/Roaming/bower/cache/" : "${userHome}/.cache/bower/"
        }
        new File(cachePath)
    }
    static void setTaskDependencies(Project project) {
        INSTALL_DEPENDENT_TASKS.each { String taskName ->
            Task task = project.tasks.findByName(taskName)
            if (task) {
                task.dependsOn INSTALL_TASK
            }
        }
    }

}
