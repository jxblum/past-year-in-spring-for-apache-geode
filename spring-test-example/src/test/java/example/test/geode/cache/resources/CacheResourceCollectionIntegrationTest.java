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
package example.test.geode.cache.resources;

import static example.app.model.User.newUser;
import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.model.User;
import example.app.repo.UserRepository;

/**
 * Example Unit/Integration Test class using Spring Boot with Apache Geode.
 *
 * 1. Test STDG @EnableGemFireResourceCollector annotation functionality
 * 2. Mock unsupported GemFire cache (Region) operations
 * 3. Configure @EnableGemFireMockObjects annotation, destroyOnEvents attribute
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.gemfire.tests.integration.IntegrationTestsSupport
 * @see org.springframework.data.gemfire.tests.integration.annotation.EnableGemFireResourceCollector
 * @see org.springframework.test.context.junit4.SpringRunner
 * <a href="https://github.com/spring-projects/spring-test-data-geode#stdg-in-a-nutshell">STDG reference documentation</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CacheResourceCollectionIntegrationTest.TestGeodeConfiguration.class)
@SuppressWarnings("unused")
public class CacheResourceCollectionIntegrationTest extends IntegrationTestsSupport {

	@Autowired
	private GemfireTemplate usersTemplate;

	@Resource(name = "Users")
	private Region<Long, User> users;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void saveAndFindUser() {

		User jonDoe = newUser(1L, "Jon Doe");

		assertThat(this.userRepository.count()).isZero();
		assertThat(this.users.size()).isZero();

		this.userRepository.save(jonDoe);

		assertThat(this.userRepository.count()).isOne();
		assertThat(this.users.size()).isOne();

		User jonDoeById = this.userRepository.findById(jonDoe.getId()).orElse(null);

		assertThat(jonDoeById).isNotNull();
		assertThat(jonDoeById).isEqualTo(jonDoe);
	}

	@CacheServerApplication
	@EnableLocator
	@EnableManager(start = true)
	@EnableEntityDefinedRegions(basePackageClasses = User.class, serverRegionShortcut = RegionShortcut.LOCAL_PERSISTENT)
	@EnableGemfireRepositories(basePackageClasses = UserRepository.class)
	@EnablePdx(persistent = true)
	static class TestGeodeConfiguration {

		@Bean
		@DependsOn("Users")
		GemfireTemplate usersTemplate(GemFireCache cache) {
			return new GemfireTemplate(cache.getRegion("/Users"));
		}
	}
}
