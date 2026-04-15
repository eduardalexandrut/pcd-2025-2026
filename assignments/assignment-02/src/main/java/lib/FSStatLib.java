package lib;

import java.nio.file.Path;

public interface FSStatLib {
    void getFSReport(Path dir, long maxFS, int nb, FSUpdateListener listener);

    void stop();
}
