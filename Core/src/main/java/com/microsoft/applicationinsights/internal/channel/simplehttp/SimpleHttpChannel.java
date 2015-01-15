package com.microsoft.applicationinsights.internal.channel.simplehttp;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.internal.logger.InternalLogger;
import com.microsoft.applicationinsights.telemetry.JsonTelemetryDataSerializer;
import com.microsoft.applicationinsights.telemetry.Telemetry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * A simple HTTP channel, using no buffering, batching, or asynchrony.
 */
final class SimpleHttpChannel implements TelemetryChannel
{
    @Override
    public boolean isDeveloperMode()
    {
        return developerMode;
    }

    @Override
    public void setDeveloperMode(boolean value)
    {
        developerMode = value;
    }

    public SimpleHttpChannel(TelemetryConfiguration configuration) {
    }

    @Override
    public void send(Telemetry item)
    {
        try
        {
            // Establish the payload.
            StringWriter writer = new StringWriter();
//            item.serialize(new JsonWriter(writer));
            item.serialize(new JsonTelemetryDataSerializer(writer));

            // Send it.

            String payload = writer.toString();

            if (developerMode) {
                InternalLogger.INSTANCE.log("SimpleHttpChannel, payload: %s", payload);
            }

            HttpPost request = new HttpPost("https://dc.services.visualstudio.com/v2/track");
            StringEntity body = new StringEntity(payload, ContentType.create("application/x-json-stream"));
            request.setEntity(body);

            CloseableHttpClient httpClient = HttpClients.createDefault();

            CloseableHttpResponse response = null;
            try
            {
                response = httpClient.execute(request);
                HttpEntity respEntity = response.getEntity();
                if (respEntity != null)
                    respEntity.getContent().close();

                if (developerMode) {
                    InternalLogger.INSTANCE.log("SimpleHttpChannel, response: %s", response.getStatusLine());
                }
            }
            catch (IOException ioe)
            {
                try
                {
                    if (response != null)
                    {
                        response.close();
                    }
                }
                catch (IOException ioeIn)
                {
                }
            }
        }
        catch (IOException ioe)
        {
        }
    }

    @Override
    public void stop(long timeout, TimeUnit timeUnit) {
    }

    private boolean developerMode = false;
}
