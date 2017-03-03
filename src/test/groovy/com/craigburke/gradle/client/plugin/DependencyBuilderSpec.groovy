package com.craigburke.gradle.client.plugin

import com.craigburke.gradle.client.dependency.Dependency
import org.gradle.api.GradleException
import spock.lang.Specification
import spock.lang.Subject

class DependencyBuilderSpec extends Specification {

    @Subject DependencyBuilder builder

    def setup() {
        builder = new DependencyBuilder()
    }

    def "builder can build a simple dependency"() {
        setup:
        dependencies.delegate = builder

        when:
        dependencies.call()

        then:
        formatDependencies(builder.rootDependencies) == ['foo@1.0.0']

        where:
        dependencies = {
            foo('1.0.0')
        }
    }

    def "builder can build a multiple simple dependencies"() {
        setup:
        dependencies.delegate = builder

        when:
        dependencies.call()

        then:
        formatDependencies(builder.rootDependencies) == ['bar@1.2.0', 'baz@2.0.0', 'foo@1.0.0']

        where:
        dependencies = {
            foo('1.0.0')
            bar('1.2.0')
            baz('2.0.0')
        }
    }

    def "builder can add a dependency with a git url"() {
        setup:
        dependencies.delegate = builder

        when:
        dependencies.call()
        Dependency dependency = builder.rootDependencies.first()

        then:
        dependency.name == 'foo'

        and:
        dependency.versionExpression == '1.0.0'

        and:
        dependency.fullUrl == 'http://www.example.com/foo.git'

        where:
        dependencies = {
            foo('1.0.0', url: 'http://www.example.com/foo.git')
        }
    }

    def "builder can add all dependencies from bower.json file (string)"() {
        setup:
        dependencies.delegate = builder

        when:
        dependencies.call()

        then:
        formatDependencies(builder.rootDependencies) == ['bar@1.2.0', 'baz@2.0.0', 'foo@1.0.0']

        where:
        dependencies = {
            bowerFile "src/test/resources/bowerFiles/bower1.json"
        }
    }

	def "builder can add all dependencies from bower.json file (file)"() {
		setup:
		dependencies.delegate = builder

		when:
		dependencies.call()

		then:
		formatDependencies(builder.rootDependencies) == ['bar@1.2.0', 'baz@2.0.0', 'foo@1.0.0']

		where:
		dependencies = {
			bowerFile new File("src/test/resources/bowerFiles/bower1.json")
		}
	}

	def "should throw GradleException when file does not exists"() {
		setup:
		dependencies.delegate = builder

		when:
		dependencies.call()

		then:
		thrown GradleException

		where:
		dependencies = {
			bowerFile new File("does not exists")
		}
	}

	def "should throw GradleException when bower file does not have dependencies"() {
		setup:
		dependencies.delegate = builder

		when:
		dependencies.call()

		then:
		thrown GradleException

		where:
		dependencies = {
			bowerFile new File("src/test/resources/bowerFiles/bower_wrong_file.json")
		}
	}

	def "should throw GradleException when passed null"() {
		setup:
		dependencies.delegate = builder

		when:
		dependencies.call()

		then:
		thrown GradleException

		where:
		dependencies = {
			bowerFile null
		}
	}

    List<String> formatDependencies(List<Dependency> dependencies) {
        dependencies.collect { "${it.name}@${it.versionExpression}" }.sort()
    }

}
