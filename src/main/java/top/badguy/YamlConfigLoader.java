package top.badguy;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class YamlConfigLoader {

    public static String ACCESS_KEY_ID = null;

    public static String ACCESS_KEY_SECRET = null;

    public static String RR = null;
    public static String DOMAIN = null;


    public static void init() {
        if (ACCESS_KEY_ID != null) {
            return;
        }
        Yaml yaml = new Yaml(new Constructor(Map.class));
        Map<String, Object> data = null;
        try {
            File configFile = new File(System.getProperty("user.dir") + File.separator + "application.yml");
            InputStream inputStream = new FileInputStream(configFile);
            data = yaml.load(inputStream);
        } catch (Exception e) {
            System.out.println("Error in reading YAML file");
            throw new RuntimeException(e);
        }

        System.out.println(data);
        Map<String, Object> app = (Map<String, Object>) data.get("aliyun");
        //aliyun:
        //  dns:
        //    access-key-id: xxx
        //    access-key-secret: xxx
        if (app != null) {
            Map<String, Object> dns = (Map<String, Object>) app.get("dns");
            if (dns != null) {
                ACCESS_KEY_ID = (String) dns.get("access-key-id");
                ACCESS_KEY_SECRET = (String) dns.get("access-key-secret");
            }
        }

        //domain:
        //  rr: v2
        //  domain: xxx.com
        Map<String, Object> domain = (Map<String, Object>) data.get("domain");
        if (domain != null) {
            RR = (String) domain.get("rr");
            DOMAIN = (String) domain.get("domain");
        }
    }
}
