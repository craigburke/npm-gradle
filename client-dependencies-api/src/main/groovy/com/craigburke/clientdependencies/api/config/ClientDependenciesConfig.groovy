package com.craigburke.clientdependencies.api.config

import com.craigburke.clientdependencies.api.dependency.Dependency
import com.craigburke.clientdependencies.api.dependency.DependencyBuilder
import com.craigburke.clientdependencies.api.registry.bower.BowerRegistry
import com.craigburke.clientdependencies.api.registry.core.DependencyResolveException
import com.craigburke.clientdependencies.api.registry.core.Registry
import com.craigburke.clientdependencies.api.registry.npm.NpmRegistry
import com.craigburke.clientdependencies.api.registry.npm.YarnRegistry
import org.slf4j.Logger

/**
 *
 * Dependencies config
 *
 */
class ClientDependenciesConfig {

    List<Registry> registries = []
    List<Dependency> rootDependencies = []

    boolean useGlobalCache = true
    boolean checkDownloads = true

    String githubUsername
    String githubPassword
    String githubToken
    String userAgent = 'client-dependencies-api'

    File installDir
    File cacheDir

    private final List<Object> fileExtensions = ['css', 'js', 'eot', 'svg', 'ttf', 'woff', 'woff2', 'ts',
                                                 'jpg', 'jpeg', 'png', 'gif']

    private final List<Object> releaseFolders = ['dist', 'release']

    private final List<Object> copyIncludes = []
    private final List<Object> copyExcludes = ['**/*.min.js', '**/*.min.css', '**/*.map', '**/Gruntfile.js',
                                               'gulpfile.js', 'source/**']

    Closure defaultCopy
    Logger logger

    ClientDependenciesConfig(Logger logger = null) {
        this.logger = logger
        this.registries = [
                new BowerRegistry(),
                new NpmRegistry(),
                new YarnRegistry()
        ]
    }

    void setInstallDir(Object dir) {
        File realDir = null
        if (dir instanceof String) {
            realDir = new File(dir)
        } else if (dir instanceof File) {
            realDir = dir
        }
        this.installDir = realDir
    }

    void setCacheDir(Object dir) {
        File realDir = null
        if (dir instanceof String) {
            realDir = new File(dir)
        } else if (dir instanceof File) {
            realDir = dir
        }
        this.cacheDir = realDir
    }

    void setFileExtensions(Object... extensions) {
        this.fileExtensions.clear()
        this.fileExtensions.addAll(extensions)
    }

    void addFileExtensions(Object... extensions) {
        this.fileExtensions.addAll(extensions)
    }

    List<String> getFileExtensions() {
        stringize(this.fileExtensions)
    }

    void setReleaseFolders(Object... extensions) {
        this.releaseFolders.clear()
        this.releaseFolders.addAll(extensions)
    }

    void addReleaseFolders(Object... extensions) {
        this.releaseFolders.addAll(extensions)
    }

    List<String> getCopyIncludes() {
        stringize(this.copyIncludes)
    }

    void setCopyIncludes(Object... includes) {
        this.copyIncludes.clear()
        this.copyIncludes.addAll(includes)
    }

    void addCopyIncludes(Object... includes) {
        this.copyIncludes.addAll(includes)
    }

    List<String> getCopyExcludes() {
        stringize(this.copyExcludes)
    }

    void setCopyExcludes(Object... includes) {
        this.copyExcludes.clear()
        this.copyExcludes.addAll(includes)
    }

    void addCopyExcludes(Object... includes) {
        this.copyExcludes.addAll(includes)
    }

    List<String> getReleaseFolders() {
        stringize(this.releaseFolders)
    }

    def methodMissing(String registryName, args) {
        if (args && args.last() instanceof Closure) {
            Registry registry = findRegistry(registryName)
            DependencyBuilder dependencyBuilder = new DependencyBuilder(registry)
            Closure clonedClosure = args.last().rehydrate(dependencyBuilder, this, this)
            clonedClosure.resolveStrategy = Closure.DELEGATE_FIRST
            clonedClosure()
            rootDependencies += dependencyBuilder.rootDependencies
        }
    }

    void registry(Map props = [:], String name) {
        String url = props.url as String
        Registry registry
        switch (props.type) {
            case 'bower':
                registry = new BowerRegistry(name, logger, url)
                break
            case 'npm':
                registry = new NpmRegistry(name, logger, url)
                break
            case 'yarn':
                registry = new YarnRegistry(name, logger, url)
                break
            default:
                throw new DependencyResolveException("Unknown Registry: $name")
        }
        registry.githubUsername = githubUsername
        registry.githubPassword = githubPassword
        registry.githubToken = githubToken
        registries += registry
    }

    Registry findRegistry(String name) {
        registries.find { it.name == name }
    }

    Closure getCopyConfig() {
        if (defaultCopy) {
            return defaultCopy
        }

        List<String> includes = fileExtensions.collect { "**/*.${it}" } + copyIncludes

        return {
            exclude copyExcludes
            include includes
        }
    }

    List<String> stringize(List<?> source) {
        if (source == null || source.empty) {
            return Collections.emptyList()
        }
        source.collect { it ? it.toString() : null }
    }
}
