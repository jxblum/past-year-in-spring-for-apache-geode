/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.boot.geode.cache.initialization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.model.User;
import example.app.repo.UserRepository;

/**
 * Integration Tests demonstrating the SBDG cache initialization feature.
 *
 * See SBDG's reference documentation on
 * <a href="https://docs.spring.io/spring-boot-data-geode-build/1.4.0-SNAPSHOT/reference/html5/#geode-data-using-export">Exporting Data</a>
 * for more information on cache data exports.
 *
 * To get the cache data export to work properly, I had to disable GemFire/Geodes JRE shutdown hook, explicitly.
 * Technically, this is handled in SBDG using a Spring Boot EnvironmentPostProcessor, but it is subject to the
 * vagaries in timing of Java's {@link ClassLoader} functionality.
 *
 * In my IDE (IJ IDEA) I did this in my run configuration for this class.
 *
 * <code>
 *     -Dgemfire.disableShutdownHook=true
 * </code>
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://docs.spring.io/spring-boot-data-geode-build/1.4.0-SNAPSHOT/reference/html5/#geode-data-using">Using Data</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CacheInitializationIntegrationTests.SpringGeodeConfiguration.class,
	properties = {
		//"spring.boot.data.gemfire.cache.data.export.enabled=true",
		//"spring.boot.data.gemfire.cache.data.export.resource.location=file://#{#env['user.home']}/pivdev/springone-2020/past-year-in-spring-for-apache-geode/spring-boot-example/tmp/my-#{#regionName}.json"
	}
)
@SuppressWarnings("unused")
public class CacheInitializationIntegrationTests extends IntegrationTestsSupport {

	@Autowired
	private UserRepository userRepository;

	@Test
	public void usersRegionsContainsDoeFamily() {

		this.userRepository.save(User.newUser(6L, "Fro Doe"));
		this.userRepository.save(User.newUser(4L, "Lan Doe"));

		assertThat(this.userRepository.findAll()).containsExactlyInAnyOrder(
			User.newUser(1L, "Jon Doe"),
			User.newUser(2L, "Jane Doe"),
			User.newUser(3L, "Cookie Doe"),
			User.newUser(4L, "Lan Doe"),
			User.newUser(5L, "Sour Doe"),
			User.newUser(6L, "Fro Doe")
		);
	}

	@SpringBootApplication
	@EnableClusterAware
	@EnableEntityDefinedRegions(basePackageClasses = User.class)
	@EnableGemfireRepositories(basePackageClasses = UserRepository.class)
	static class SpringGeodeConfiguration { }

}
