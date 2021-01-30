package demo.steve;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeWeathermanIT extends WeathermanTest {

    // Execute the same tests but in native mode.
}