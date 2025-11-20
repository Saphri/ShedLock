package net.javacrumbs.shedlock.provider.nats.jetstream;

import static org.assertj.core.api.Assertions.assertThat;

import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.JetStreamApiException;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.nats.client.api.KeyValueEntry;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.test.support.AbstractLockProviderIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class NatsJetStreamLockProviderIntegrationTest extends AbstractLockProviderIntegrationTest {

    @Container
    public static final NatsJetStreamContainer container = new NatsJetStreamContainer();

    private LockProvider lockProvider;
    private Connection connection;

    @BeforeEach
    public void createLockProvider() throws Exception {
        var natsUrl = String.format("nats://%s:%d", container.getHost(), container.getFirstMappedPort());
        connection = Nats.connect(Options.builder()
            .server(natsUrl)
            .connectionListener(new ConnectionListener() {

                private final Logger log = LoggerFactory.getLogger("ConnectionListener");

                @Override
                public void connectionEvent(Connection conn, Events type) {
                    log.debug("Received event: {}, on conn: {}", type, conn);
                }
            })
            .build());

        lockProvider = new NatsJetStreamLockProvider(connection);
    }

    @AfterEach
    public void stopLockProvider() throws Exception {
        connection.close();
    }

    @Override
    protected void assertUnlocked(String lockName) {
        assertThat(getLock(lockName)).isNull();
    }

    @Override
    protected void assertLocked(String lockName) {
        assertThat(getLock(lockName)).isNotNull();
    }

    @Override
    protected LockProvider getLockProvider() {
        return lockProvider;
    }

    private KeyValueEntry getLock(String lockName) {
        try {
            return connection.keyValue("shedlock-locks").get(lockName);
        } catch (JetStreamApiException e) {
            if (e.getApiErrorCode() == 10059) { // Key not found
                return null;
            }
            throw new IllegalStateException(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
