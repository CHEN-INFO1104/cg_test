package org.gengine.content.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gengine.content.ContentWorker;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.transform.AbstractContentTransformerWorker;
import org.gengine.error.GengineRuntimeException;

/**
 * Boostrap class which loads a properties file from the path
 * specified via command line argument, loads a worker class,
 * then finds a corresponding component bootstrap and runs it
 * which creates an AMQP endpoint and starts a listener for it.
 */
public class SimpleAmqpNodeBootstrap
{
    protected static final String PROP_WORKER_CLASS = "gengine.worker.class";

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            throw new IllegalArgumentException("USAGE: propertiesFilePath");
        }

        Properties properties = loadProperties(args[0]);

        ContentWorker worker = (ContentWorker) createObjectFromClassInProperties(
                properties, PROP_WORKER_CLASS);

        @SuppressWarnings("rawtypes")
        AbstractComponentBootstrapFromProperties componentBootrap =
            getComponentBootrap(worker, properties);

        componentBootrap.run();
    }

    /**
     * Loads the properties from the path specified via command line.
     *
     * @param propertiesFilePath
     * @return the properties object
     */
    protected static Properties loadProperties(String propertiesFilePath)
    {
        Properties properties = null;
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(propertiesFilePath);
            properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
        catch (IOException e)
        {
            throw new GengineRuntimeException("Could not load required " + propertiesFilePath);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException e)
            {

            }
        }
    }

    /**
     * Attempts to load the class specified in the properties file and
     * create a new instance of it.
     *
     * @return the newly created object
     */
    protected static Object createObjectFromClassInProperties(Properties properties, String key)
    {
        try
        {
            String workerClassName = properties.getProperty(key);
            Class<?> workerClass = SimpleAmqpNodeBootstrap.class.getClassLoader().loadClass(
                            workerClassName);
            return workerClass.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new GengineRuntimeException("Could not load class", e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static <W extends ContentWorker> AbstractComponentBootstrapFromProperties<W> getComponentBootrap(
            W worker, Properties properties)
    {
        if (worker instanceof AbstractContentTransformerWorker)
        {
            return new TransformerComponentBootstrapFromProperties(
                    properties, (AbstractContentTransformerWorker) worker);
        }
        if (worker instanceof AbstractContentHashWorker)
        {
            return new HashComponentBootstrapFromProperties(
                    properties, (AbstractContentHashWorker) worker);
        }
        return null;
    }

}
