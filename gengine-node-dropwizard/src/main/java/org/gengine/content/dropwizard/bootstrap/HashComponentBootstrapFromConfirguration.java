package org.gengine.content.dropwizard.bootstrap;

import io.dropwizard.setup.Environment;

import org.gengine.content.dropwizard.configuration.NodeConfiguration;
import org.gengine.content.dropwizard.health.HashHealthCheck;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.hash.BaseContentHashComponent;
import org.gengine.content.hash.ContentHashWorker;
import org.gengine.error.ChenInfoRuntimeException;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

import com.codahale.metrics.health.HealthCheck;

/**
 * Bootraps a hash component
 *
 */
public class HashComponentBootstrapFromConfirguration
        extends AbstractComponentBootstrapFromConfiguration<BaseContentHashComponent, ContentHashWorker>
{
    public HashComponentBootstrapFromConfirguration(
            NodeConfiguration nodeConfig, Environment environment, ContentHashWorker worker)
    {
        super(nodeConfig, environment, worker);
    }

    protected static final String PROP_WORKER_DIR_TARGET = "gengine.worker.dir.target";

    @Override
    protected BaseContentHashComponent createComponent()
    {
        return new BaseContentHashComponent();
    }

    protected void initWorker()
    {
        if (!(worker instanceof AbstractContentHashWorker))
        {
            throw new ChenInfoRuntimeException(
                    "Only " + AbstractContentHashWorker.class.getSimpleName() + " supported");
        }
        ((AbstractContentHashWorker) worker).setSourceContentReferenceHandler(
                createFileContentReferenceHandler(nodeConfig.getSourceDirectory()));
        ((AbstractContentHashWorker) worker).initialize();
    }

    @Override
    public HealthCheck getHealthCheck(BaseContentHashComponent component, AmqpDirectEndpoint endpoint)
    {
        return new HashHealthCheck(component, endpoint);
    }

}
