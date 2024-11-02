package net.javacrumbs.shedlock.provider.nats.jetstream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class NatsJetStreamContainer extends GenericContainer<NatsJetStreamContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NatsJetStreamContainer.class);

    public static final DockerImageName NATS_IMAGE = DockerImageName.parse("nats:2.10-alpine");

    private static final Integer NATS_PORT = 4222;
    private static final Integer NATS_HTTP_PORT = 8222;

    public NatsJetStreamContainer() {
        super(NATS_IMAGE.asCanonicalNameString());
        this.withExposedPorts(NATS_PORT, NATS_HTTP_PORT)
            .withLogConsumer(frame -> LOGGER.info(frame.getUtf8String()))
            .withCommand("--jetstream", "--http_port", NATS_HTTP_PORT.toString());
    }
}
