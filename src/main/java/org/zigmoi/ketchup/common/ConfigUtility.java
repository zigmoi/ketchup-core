package org.zigmoi.ketchup.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.zigmoi.ketchup.exception.KConfigurationException;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Configuration
public class ConfigUtility {

    @Autowired
    private static Environment env;

    private static ConfigUtility utility;

    private static Map<String, String> props = new LinkedHashMap<>();

    private final static String replaceBeginCharSeq = "\\$\\{";
    private final static String replaceEndCHarSeq = "}";
    private final static Pattern enclosurePattern = Pattern.compile("\\$\\{(.*?)\\}");

    public ConfigUtility() {
    }

    public synchronized static ConfigUtility instance() {
        if (utility == null) {
            utility = new ConfigUtility();
            if (env != null) {
                MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
                StreamSupport.stream(propSrcs.spliterator(), false)
                        .filter(ps -> ps instanceof EnumerablePropertySource)
                        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                        .flatMap(Arrays::stream)
                        .forEach(propName -> props.put(propName, env.getProperty(propName)));
            } else {
                props.putAll(System.getenv());
                props.putAll(getJVMProperties());
                try (InputStream input = ConfigUtility.class.getClassLoader().getResourceAsStream("application.properties")) {
                    Properties p = new Properties();
                    if (input == null) {
                        throw new KConfigurationException("File application.properties not found");
                    }
                    p.load(input);
                    String profile = p.getProperty("spring.profiles.active");
                    if (profile == null || profile.trim().isEmpty()) {
                        props.putAll(convertToMapStringString(p));
                    } else {
                        String pFileName = "application-" + profile + ".properties";
                        Properties p2 = new Properties();
                        try (InputStream i = ConfigUtility.class.getClassLoader().getResourceAsStream(pFileName)) {
                            p2.load(i);
                        }
                        props = convertToMapStringString(p2);
                    }
                } catch (Exception ex) {
                    throw new KConfigurationException("Failed loading application.properties", ex);
                }
            }
        }
        props = formatConfigValues(props);
        return utility;
    }

    private static Map<String, String> convertToMapStringString(Properties p) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String k : p.stringPropertyNames()) {
            map.put(k, p.getProperty(k));
        }
        return map;
    }

    private static Map<? extends String, ? extends String> getJVMProperties() {
        Map<String, String> map = new HashMap<>();
        Properties env = System.getProperties();
        for (String envName : env.stringPropertyNames()) {
            map.put(envName, String.valueOf(env.get(envName)));
        }
        return map;
    }

    public String getProperty(String key) {
        if (!props.containsKey(key)) {
            throw new KConfigurationException("Key : " + " not found");
        }
        return props.get(key);
    }

    public String getPropertyOrDefault(String key, String defaultValue) {
        if (!props.containsKey(key)) {
            return defaultValue;
        }
        return props.get(key);
    }

    public String getTmpDir() {
        return getProperty("ketchup.dir.tmp");
    }

    public static Map<String, String> formatConfigValues(Map<String, String> map) {

        Map<String, String> res = new HashMap<>();
        for (String key : map.keySet()) {
            String value = replace(map.get(key), map);
            res.put(key, value);
        }
        return res;
    }

    // todo failed multi-nested-var config parsing ketchup.deployment-template.mvn-clean-install=${ketchup.dir.base}/conf/templates/c_mvn_clean_install.txt
    private static String replace(String value, Map<String, String> map) {
        if (enclosurePattern.matcher(value).find()) {
            List<String> valuePartKeys = getEnclosedPartsForVelocityStyleDelim(value);
            Map<String, String> replaceables = new HashMap<>();
            for (String partKey : valuePartKeys) {
                String partVal = map.get(partKey);
                replaceables.put(partKey, partVal);
            }

            for (String key : replaceables.keySet()) {
                value = value.replaceAll(replaceBeginCharSeq + key + replaceEndCHarSeq,
                        replaceables.get(key) != null ? replaceables.get(key) : "");
            }
            return replace(value, map);
        }
        return value;
    }

    public static List<String> getEnclosedPartsForVelocityStyleDelim(String str) {

        List<String> enclosedParts = new ArrayList<>();

        Matcher m = enclosurePattern.matcher(str);
        while (m.find()) {
            enclosedParts.add(m.group(1));
        }

        return enclosedParts;
    }

    public static void main(String[] args) {
        System.out.println(ConfigUtility.instance().getProperty("ketchup.dir.conf"));
    }
}
