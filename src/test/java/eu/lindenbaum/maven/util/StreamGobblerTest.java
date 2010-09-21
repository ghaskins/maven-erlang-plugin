package eu.lindenbaum.maven.util;

import static org.easymock.EasyMock.createStrictControl;

import java.io.File;
import java.io.InputStream;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public class StreamGobblerTest {
  private IMocksControl control;
  private VoidProcedure<String> processor;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    this.control = createStrictControl();
    this.processor = this.control.createMock("outputProcessor", VoidProcedure.class);
  }

  @Test
  public void testEmpty() throws Exception {
    String file = "stream-gobbler" + File.separator + "empty.txt";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);

    this.control.replay();

    StreamGobbler gobbler = new StreamGobbler(inputStream, this.processor);
    gobbler.run();

    this.control.verify();
  }

  @Test
  public void testNonEmpty() throws Exception {
    String file = "stream-gobbler" + File.separator + "non-empty.txt";
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file);

    this.processor.apply("line1");
    this.processor.apply("line2");

    this.control.replay();

    StreamGobbler gobbler = new StreamGobbler(inputStream, this.processor);
    gobbler.run();

    this.control.verify();
  }
}