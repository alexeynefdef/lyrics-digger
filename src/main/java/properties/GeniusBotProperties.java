package properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GeniusBotProperties {
    public static String getProp(String propName) {
        try {
            File appProps = new File("src/main/java/properties/application.properties");
            FileInputStream stream = new FileInputStream(appProps);
            Properties props = new Properties();
            props.load(stream);
            return props.get(propName).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
