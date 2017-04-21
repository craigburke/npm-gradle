package com.craigburke.gradle.client.registry.npm

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
    static final Logger logger = LoggerFactory.getLogger(YarnRegistry.name)
    
    YarnRegistry(String name, String url = DEFAULT_YARN_URL, List<String> configFiles = DEFAULT_YARN_FILENAMES) {
        this(name,logger,url,configFiles)
    }
    
    YarnRegistry(String name, Logger logger, String url = DEFAULT_YARN_URL, List<String> configFiles = DEFAULT_YARN_FILENAMES) {
        super(name, logger, url, configFiles)
    }
}
