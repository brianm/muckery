package org.skife.muckery.sshj;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SshjTest
{
    public static final String USER = "brianm";
    public static final String PASS = "secure!";

@Test
public void testFoo() throws Exception
{
    SshServer server = SshServer.setUpDefaultServer();
    server.setPort(2222);
    server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
    server.setPasswordAuthenticator(new AllowAnyone());
    server.setCommandFactory(new HelloWorldCommandFactory());
    server.start();


    SSHClient ssh = new SSHClient();
    ssh.addHostKeyVerifier(new PromiscuousVerifier());

    ssh.connect("localhost", 2222);

    ssh.authPassword("brianm", "abc123");
    Session s = ssh.startSession();

    Session.Command cmd = s.exec("ls -l /");
    cmd.join();


    String out = CharStreams.toString(
        new InputStreamReader(cmd.getInputStream(),
                              Charsets.UTF_8));

    assertThat(out).isEqualTo("Hello, world!");
    s.close();
    ssh.close();

    server.stop();
}


private static class AllowAnyone implements PasswordAuthenticator
{

    @Override
    public boolean authenticate(final String username, final String password, final ServerSession session)
    {
        return true;
    }
}

private class HelloWorldCommandFactory implements CommandFactory
{
    @Override
    public Command createCommand(final String command)
    {
        return new Command()
        {

            public InputStream in;
            public OutputStream out;
            public OutputStream err;
            public ExitCallback exit;

            @Override
            public void setInputStream(final InputStream in)
            {
                this.in = in;
            }

            @Override
            public void setOutputStream(final OutputStream out)
            {
                this.out = out;
            }

            @Override
            public void setErrorStream(final OutputStream err)
            {
                this.err = err;
            }

            @Override
            public void setExitCallback(final ExitCallback callback)
            {
                this.exit = callback;
            }

            @Override
            public void start(final Environment env) throws IOException
            {
                out.write("Hello, world!".getBytes(Charsets.US_ASCII));
                out.flush();
                exit.onExit(0);
            }

            @Override
            public void destroy()
            {
            }
        };
    }
}
}
