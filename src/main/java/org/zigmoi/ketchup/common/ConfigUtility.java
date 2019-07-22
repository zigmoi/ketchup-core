package org.zigmoi.ketchup.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

@Configuration
public class ConfigUtility {

    @Autowired
    private static Environment env;

    private static ConfigUtility utility;

    private static Properties props = new Properties();

    public ConfigUtility() {}

    public synchronized static ConfigUtility instance() {
        if (utility == null) {
            utility = new ConfigUtility();
            MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
            StreamSupport.stream(propSrcs.spliterator(), false)
                    .filter(ps -> ps instanceof EnumerablePropertySource)
                    .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                    .flatMap(Arrays::stream)
                    .forEach(propName -> props.setProperty(propName, env.getProperty(propName)));
        }
        return utility;
    }

    public String getDataOutDirPath(String basepath, String... paths) {
        return null;
    }

    public String getPath(String basepath, String... paths) {
        return null;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(ConfigUtility.instance().getProperty("ketchup.dir.conf"));
    }
}
