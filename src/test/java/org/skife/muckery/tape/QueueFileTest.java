package org.skife.muckery.tape;

import com.google.common.base.Charsets;
import com.squareup.tape.QueueFile;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueFileTest
{
    @Test
    public void testFoo() throws Exception
    {
Path tmp = Files.createTempFile("queue", ".tmp");
Files.delete(tmp);

QueueFile qf = new QueueFile(tmp.toFile());

qf.add(new byte[]{1, 2, 3});
qf.add(new byte[]{4, 5, 6});

assertThat(qf.peek()).isEqualTo(new byte[]{1, 2, 3});
qf.remove();

assertThat(qf.peek()).isEqualTo(new byte[]{4, 5, 6});
qf.remove();

assertThat(qf.peek()).isNull();

qf.close();
Files.delete(tmp);
    }
}
