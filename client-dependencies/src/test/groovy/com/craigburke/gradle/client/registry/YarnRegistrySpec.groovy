package com.craigburke.gradle.client.registry

import com.craigburke.clientdependencies.api.registry.npm.YarnRegistry

class YarnRegistrySpec extends AbstractRegistrySpec {

    def setup() {
        init(YarnRegistry, 'npm')
    }
}
