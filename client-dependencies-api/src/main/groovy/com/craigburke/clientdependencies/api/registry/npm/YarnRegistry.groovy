package com.craigburke.clientdependencies.api.registry.npm

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * Registry to resolves Yarn NPM Dependencies
 *
 * @author Craig Burke
 */
class YarnRegistry extends NpmRegistry {

    static final String DEFAULT_YARN_URL = 'https://registry.yarnpkg.com'
    static final List<String> DEFAULT_YARN_FILENAMES = ['yarn.json'] + DEFAULT_NPM_FILENAMES
    static final Logger LOGGER = LoggerFactory.getLogger(YarnRegistry.name)

    YarnRegistry(String name = 'yarn', Logger logger = null,
                 String url = DEFAULT_YARN_URL, List<String> configFiles = DEFAULT_YARN_FILENAMES) {
        super(name, logger ?: LOGGER, url, configFiles)
    }
}
