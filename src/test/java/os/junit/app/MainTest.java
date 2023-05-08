package os.junit.app;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import os.exercise.app.Main;

/**
 * Unitary tests for the Main class.
 */
class MainTest {

    @Test
    void givenAValidPathWhenGetPathsThenSuccess(){
        Path folderPath = Paths.get("src/test/resources");
        List<Path> paths = Main.getPaths(folderPath);

        long count = -1;
        try (Stream<Path> countingPaths = Files.list(folderPath)) {
            count = countingPaths.count();
        } catch (Exception e) {
            ExceptionUtils.readStackTrace(e);
        }

        assertEquals(count, paths.size());
        for (Path path : paths) {
            assertTrue(path.toString().endsWith(".json"));
        }
    }

    @Test
    void givenAnInvalidPathWhenGetPathsThenNumberOfFilesEqualsZero() {
        Path folderPath = Paths.get("src/test/resources/non-existing-folder");
        List<Path> paths = Main.getPaths(folderPath);
        assertEquals(0, paths.size());
    }
}
