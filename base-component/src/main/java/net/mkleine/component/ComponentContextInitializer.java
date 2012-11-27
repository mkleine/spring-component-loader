package net.mkleine.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *   <components:load id="components" verbose="true" system-properties="override">

 Loading order (first has higher precedence than last):
 1. system properties
 2. jndi entries from java:comp/env/<your_framework_name>/application as properties
 3. application.xml/application.properties (if available)
 4. component-*.xml / component-*.properties from /WEB-INF/
 5. component-*.xml / component-*.properties from /META-INF/<your_framework_name>/

 <components:resource type="properties" location="jndi:java:comp/env/<your_framework_name>/application/"/>
 <components:resource type="properties" location="/WEB-INF/application.properties"/>
 <components:resource type="beans" location="/WEB-INF/application.xml"/>

 <components:component name="*"/>
 <components:directory location="/WEB-INF"/>
 <components:directory location="classpath*:/META-INF/<your_framework_name>"/>

 </components:load>
 */
@SuppressWarnings("UnusedDeclaration")
public class ComponentContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final Logger LOG = LoggerFactory.getLogger(ComponentContextInitializer.class);

  static final String[] configLocations = new String[] {
          "/WEB-INF/application.xml",
          "/WEB-INF/component-*.xml",
          "classpath*:/META-INF/spring_components/component-*.xml"
  };

  static final String[] propertyLocations = new String[] {
          "/WEB-INF/application.properties",
          "/WEB-INF/component-*.properties",
          "classpath*:/META-INF/spring_components/component-*.properties"
  };

  private void configureContextLocations(ConfigurableWebApplicationContext context) {
    final List<String> existingConfigLocations = new ArrayList<String>(configLocations.length);
    for(String configLocation : configLocations) {
      try {
        final Resource[] resources = context.getResources(configLocation);
        for(Resource resource : resources) {
          if(resource.exists()) {
            existingConfigLocations.add(configLocation);
            LOG.info("Adding config location " + configLocation);
            break;
          }
        }
      } catch (IOException e) {
        LOG.info("Ignoring config location " + configLocation);
      }
    }
    final String[] locations = new String[existingConfigLocations.size()];
    existingConfigLocations.toArray(locations);
    context.setConfigLocations(locations);
  }

  private void configurePropertySources(ConfigurableApplicationContext context) {
    // modify property sources (add our behind the existing system, servlet context and JNDI properties
    final MutablePropertySources sources = context.getEnvironment().getPropertySources();
    for(String propertyLocation : propertyLocations) {
      try {
        final Resource[] resources = context.getResources(propertyLocation);
        for(Resource resource : resources) {
          if(resource.exists()) {
            sources.addLast(new ResourcePropertySource(resource));
          }
        }
      } catch (IOException e) {
        LOG.warn("cannot load properties from " + propertyLocation, e);
      }
    }


    if(context.getEnvironment().getProperty("net.mkleine.component.verbose", Boolean.class, false)){
      // dump properties
      final Iterable<PropertySource<?>> iterator = new Iterable<PropertySource<?>>() {
        @Override
        public Iterator<PropertySource<?>> iterator() {
          return sources.iterator();
        }

      };
      LOG.info("Current application property sources: ");
      for (PropertySource<?> propertySource : iterator) {
        LOG.info("------------------------------------------------------------------------");
        LOG.info(propertySource.toString());
        LOG.info("------------------------------------------------------------------------");
      }
    }
  }

  private static void addIfExists(List<Resource> resources, Resource candidate) {
    if( candidate.exists() ) {
      LOG.info("Using resource {}", candidate);
      resources.add(candidate);
    }
    else {
      LOG.info("No resource found at {}", candidate);
    }
  }

  @Override
  public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
    LOG.warn("--------------------------------customizeContext ------------------------");
    configureContextLocations((ConfigurableWebApplicationContext) configurableApplicationContext);
    configurePropertySources(configurableApplicationContext);
  }
}
