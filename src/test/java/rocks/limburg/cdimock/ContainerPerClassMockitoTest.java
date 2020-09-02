/*
 * Copyright 2020 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rocks.limburg.cdimock;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@MockitoInjection
@CdiExclude(classes = { MockConfigurationProvider.class, ContainerPerExecutionTest.class }, classesAnnotatedWith = OwbClass.class)
@DisplayName("Test that starts one container per test class with mockito")
class ContainerPerClassMockitoTest {

    private static SeContainer cdiContainer;
    private CreationalContext<ContainerPerClassMockitoTest> creationalContext;
    private InjectionTarget<ContainerPerClassMockitoTest> injectionTarget;

    @Mock
    private Configuration mockConfiguration;

    @Inject
    private HelloService helloService;

    @BeforeAll
    static void startCdiContainer() {
        cdiContainer = SeContainerInitializer.newInstance().initialize();
    }

    @AfterAll
    static void closeCdiContainer() {
        cdiContainer.close();
    }

    @BeforeEach
    void inject() {
        BeanManager beanManager = cdiContainer.getBeanManager();
        AnnotatedType<ContainerPerClassMockitoTest> annotatedType = beanManager.createAnnotatedType(ContainerPerClassMockitoTest.class);
        injectionTarget = beanManager.createInjectionTarget(annotatedType);
        creationalContext = beanManager.createCreationalContext(null);
        injectionTarget.inject(this, creationalContext);
        injectionTarget.postConstruct(this);
    }

    @AfterEach
    void destroy() {
        injectionTarget.preDestroy(this);
        injectionTarget.dispose(this);
    }

    @Test
    void hello() {
        when(mockConfiguration.getDefaultGreeting()).thenReturn("mockito");
        assertEquals("hello mockito", helloService.hello(empty()));
    }

    @Test
    @DisplayName("every test gets its own mock instance")
    void differentMockInstancesInjectedPerTest() {
        when(mockConfiguration.getDefaultGreeting()).thenReturn("new mock instance");
        assertEquals("hello new mock instance", helloService.hello(empty()));
    }
}
