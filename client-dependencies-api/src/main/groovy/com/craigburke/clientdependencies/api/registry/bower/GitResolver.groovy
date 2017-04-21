package com.craigburke.clientdependencies.api.registry.bower

import com.craigburke.clientdependencies.api.dependency.Dependency
import com.craigburke.clientdependencies.api.dependency.Version
import com.craigburke.clientdependencies.api.registry.core.Resolver
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.ResetOp
import org.eclipse.jgit.storage.file.WindowCacheConfig
import org.slf4j.Logger

/**
 *
 * General git resolver for bower
 *
 * @author Craig Burke
 */
class GitResolver implements Resolver {

    private final Logger log

    GitResolver(Logger log) {
        this.log = log

        // Protect against git .pack file locking on Windows,
        // see https://github.com/ajoberstar/grgit/issues/33
        WindowCacheConfig config = new WindowCacheConfig()
        config.setPackedGitMMAP(true)
        config.install()
    }

    @Override
    boolean canResolve(Dependency dependency) {
        (dependency != null)
    }

    @Override
    List<Version> getVersionList(Dependency dependency) {
        List<Version> versionList = []
        Grgit repo = Grgit.open(dir: dependency.sourceDir)
        try {
            versionList = repo.tag.list().collect { Version.parse(it.name as String) }
        } finally {
          repo.close()
        }

        versionList
    }

    @Override
    void resolve(Dependency dependency) {
        Grgit repo = Grgit.open(dir: dependency.sourceDir)
        try {
            List tags = repo.tag.list()
            if (tags) {
                String commit = tags.find { (it.name - 'v') == dependency.version.fullVersion }.commit.id
                repo.reset(commit: commit, mode: ResetOp.Mode.HARD)
            }
        } finally {
          repo.close()
        }
    }

    @Override
    void afterInfoLoad(Dependency dependency) {
        Grgit.clone(dir:  dependency.sourceDir, uri: dependency.fullUrl).close()
    }

}
