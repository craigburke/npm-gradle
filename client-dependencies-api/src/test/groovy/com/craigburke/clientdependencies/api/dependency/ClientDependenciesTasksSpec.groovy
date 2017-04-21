package com.craigburke.clientdependencies.api.dependency

import com.craigburke.clientdependencies.api.config.ClientDependenciesConfig
import com.craigburke.clientdependencies.api.config.ClientDependenciesTasks
import spock.lang.Specification

class ClientDependenciesTasksSpec extends Specification {
    void 'check config defaults'() {
        given:
            def config = new ClientDependenciesConfig()
        expect:
            config.installDir == null
            config.cacheDir == null
            config.registries.empty
    }

    void 'check task defaults'() {
        given:
            def config = new ClientDependenciesConfig()
            def tasks = new ClientDependenciesTasks(config)
        expect:
            config.installDir != null
            config.cacheDir != null
            config.installDir.exists()
            config.cacheDir.exists()
            config.registries.empty
        when:
            tasks.clean()
        then:
            !config.cacheDir.exists()
            !config.installDir.exists()
    }

    void 'download from bower'() {
        given:
            def config = new ClientDependenciesConfig()
            config.bower {
                jquery('1.11.3') {

                }
            }
            def tasks = new ClientDependenciesTasks(config)
        expect:
            !tasks.clientDependenciesAreInstalled()
        when:
            tasks.install()
        then:
            tasks.clientDependenciesAreInstalled()
        when:
            tasks.clean()
        then:
            !tasks.clientDependenciesAreInstalled()
    }
}
