package com.craigburke.clientdependencies.api.config

import static groovyx.gpars.GParsPool.withExistingPool

import com.craigburke.clientdependencies.api.dependency.Dependency
import com.craigburke.clientdependencies.api.dependency.VersionResolver
import com.craigburke.clientdependencies.api.registry.core.AbstractRegistry
import com.craigburke.clientdependencies.api.registry.core.Registry
import com.craigburke.clientdependencies.api.registry.npm.NpmRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * All client dependencies tasks
 */
class ClientDependenciesTasks {
    static final Logger LOGGER = LoggerFactory.getLogger(ClientDependenciesTasks.name)

    ClientDependenciesConfig config
    AntBuilder antBuilder
    boolean offline
    Logger log
    int threadPoolSize = 15

    ClientDependenciesTasks(ClientDependenciesConfig config, Logger logger = null) {
        this.config = config
        this.antBuilder = new AntBuilder()
        this.offline = System.getProperty('clientdependencies.offline') ? true : false
        this.log = logger ?: LOGGER

        setDefaults()
    }

    void clean() {
        antBuilder.delete(includeEmptyDirs: true) {
            fileset(dir: config.installDir.absolutePath)
            fileset(dir: config.cacheDir.absolutePath)
        }
    }

    void install() {
        installDependencies(config.rootDependencies)
    }

    void report(Logger reportLogger = null) {
        Logger logger = reportLogger
        if (logger == null) {
            logger = LOGGER
        }

        List<Dependency> allDependencies = loadDependencies(config.rootDependencies)
        List<Dependency> finalDependencies = Dependency.flattenList(allDependencies).unique(false) { it.name }

        logger.quiet ''
        printDependencies(allDependencies, finalDependencies, 1, logger)
    }

    void refresh() {
        clean()
        install()
    }

    void printDependencies(List<Dependency> dependencies, List<Dependency> finalDependencies, int level, Logger log) {
        dependencies.each { Dependency dependency ->
            String output = ('|    ' * (level - 1)) + '+--- '
            output += "${dependency.name}@"

            if (dependency.versionExpression.contains(' ')) {
                output += "(${dependency.versionExpression})"
            } else {
                output += dependency.versionExpression
            }

            Dependency installedDependency = finalDependencies.find { it.name == dependency.name }

            if (dependency.version != installedDependency.version) {
                output += " -> ${installedDependency.version.fullVersion} (*)"
            } else if (dependency.version.fullVersion != dependency.versionExpression) {
                output += " -> ${dependency.version.fullVersion}"
            }

            log.quiet "${output}"
            if (dependency.children) {
                printDependencies(dependency.children, finalDependencies, level + 1, log)
            }
        }
    }

    void installDependencies(List<Dependency> rootDependencies) {
        AbstractRegistry.setThreadPoolSize(this.threadPoolSize)

        List<Dependency> allDependencies = loadDependencies(rootDependencies)
        List<Dependency> allDependenciesFlattened = Dependency.flattenList(allDependencies)
        List<Dependency> finalDependencies = Dependency.flattenList(allDependencies).unique(false) { it.name }

        finalDependencies.each { Dependency dependency ->
            List<Dependency> conflicts = allDependenciesFlattened
                    .findAll { it.name == dependency.name && it.version != dependency.version }
                    .findAll { !it.version.compatibleWith(dependency.version) }
                    .findAll { !VersionResolver.matches(dependency.version, it.versionExpression) }

            if (conflicts) {
                log.quiet """
                    |Version conflicts found with ${dependency} [${conflicts*.versionExpression.join(', ')}]
                """.stripMargin()
            }
        }

        withExistingPool(AbstractRegistry.pool) {
            finalDependencies.eachParallel { Dependency dependency ->
                log.info "Installing: ${dependency.name}@${dependency.version?.fullVersion}"

                Registry registry = dependency.registry
                Dependency rootDependency = rootDependencies.find { it.name == dependency.name }
                Closure copyConfig = rootDependency?.copyConfig ?: config.copyConfig
                String releaseFolder = dependency.getReleaseFolder(config.releaseFolders)

                antBuilder.copy(todir: "${registry.installDir.absolutePath}/${dependency.destinationPath}") {
                    fileset(CopyConfigConsumer.consume(
                            "${dependency.sourceDir.absolutePath}/${releaseFolder}", copyConfig
                    ))
                }
            }
        }
    }

    static class CopyConfigConsumer {
        Map fileSet

        void exclude(def pattern) {
            def excludes = pattern
            if (!(pattern instanceof Collection)) {
                excludes = [pattern]
            }
            fileSet.excludes = excludes.join(',')
        }

        void include(def pattern) {
            def includes = pattern
            if (!(pattern instanceof Collection)) {
                includes = [pattern]
            }
            fileSet.includes = includes.join(',')
        }

        static Map consume(String dir, Closure copyConfig) {
            def ccc = new CopyConfigConsumer(fileSet: [dir: dir])
            copyConfig.delegate = ccc
            copyConfig.resolveStrategy = Closure.DELEGATE_FIRST
            copyConfig.call()
            ccc.fileSet
        }
    }

    static List<Dependency> loadDependencies(List<Dependency> rootDependencies) {
        withExistingPool(AbstractRegistry.pool) {
            rootDependencies
                    .collectParallel { Dependency dependency ->
                List<String> siblings = rootDependencies.findAll { it.name != dependency.name }*.name
                dependency.exclude += siblings
                dependency.registry.loadDependency(dependency as Dependency, null)
            }
        } as List<Dependency>
    }

    boolean clientDependenciesAreInstalled() {
        config.rootDependencies.every { Dependency dependency ->
            String destinationPath = "${dependency.registry.installDir.absolutePath}/${dependency.destinationPath}"
            File destination = new File(destinationPath)
            destination.exists() && destination.listFiles()
        }
    }

    static File getDefaultGlobalCache(Registry registry) {
        boolean windows = System.getProperty('os.name').toLowerCase().contains('windows')
        String userHome = System.getProperty('user.home') as String

        String cachePath
        if (registry instanceof NpmRegistry) {
            cachePath = windows ? "${userHome}/AppData/npm-cache/" : "${userHome}/.npm/"
        } else {
            cachePath = windows ? "${userHome}/AppData/Roaming/bower/cache/" : "${userHome}/.cache/bower/"
        }
        new File(cachePath)
    }

    void setDefaults() {
        if (config.installDir == null) {
            config.installDir = new File('src/assets/vendor')
            if (!config.installDir.exists()) {
                config.installDir.mkdirs()
            }
        }
        if (config.cacheDir == null) {
            config.cacheDir = new File('client-cache')
            if (!config.cacheDir.exists()) {
                config.cacheDir.mkdirs()
            }
        }
        config.registries.each { Registry registry ->
            if (registry.offline == null) {
                registry.offline = offline
            }
            if (registry.localCacheDir == null) {
                registry.localCacheDir = new File("${config.cacheDir.absolutePath}/${registry.name}/")
            }
            if (registry.globalCacheDir == null) {
                registry.globalCacheDir = registry.globalCacheDir ?: getDefaultGlobalCache(registry)
            }
            if (registry.installDir == null) {
                registry.installDir = config.installDir
            }
            if (registry.useGlobalCache == null) {
                registry.useGlobalCache = config.useGlobalCache
            }
            if (registry.checkDownloads == null) {
                registry.checkDownloads = config.checkDownloads
            }
            if (registry.githubUsername == null) {
                registry.githubUsername = config.githubUsername
            }
            if (registry.githubPassword == null) {
                registry.githubPassword = config.githubPassword
            }
            if (registry.githubToken == null) {
                registry.githubToken = config.githubToken
            }
            if (registry.userAgent == null) {
                registry.userAgent = config.userAgent
            }
        }
    }
}
