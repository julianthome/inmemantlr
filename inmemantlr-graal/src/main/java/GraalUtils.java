import java.io.File;

/**
 * Created by julian on 25/05/2017.
 */
public class GraalUtils {

    public static File getResource(String path) {
        return new File(GraalParser.class.getClassLoader().getResource
                (path).getFile());
    }
}
