package com.dc.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
// If you wish to use http then replace OtlGrpcMetricExporter to io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
// https://javadoc.io/doc/io.opentelemetry/opentelemetry-exporter-otlp/1.20.0/io/opentelemetry/exporter/otlp/http/metrics/package-summary.html
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;

import java.util.concurrent.TimeUnit;

public class OtelMetricsSender {

    public static void main(String[] args) throws InterruptedException {
        // 1. Send custom metrics via GRPC
        String endpoint = "http://localhost:4317";

        String serviceName = System.getenv("OTEL_SERVICE_NAME");
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "opentelemetry-test"; // default value
        }
        
        String serviceNamespace = System.getenv("OTEL_SERVICE_NAMESPACE");
        if (serviceNamespace == null || serviceNamespace.isEmpty()) {
            // Try to get from OTEL_RESOURCE_ATTRIBUTES if not set directly
            String resourceAttributes = System.getenv("OTEL_RESOURCE_ATTRIBUTES");
            if (resourceAttributes != null) {
                for (String attr : resourceAttributes.split(",")) {
                    if (attr.startsWith("service.namespace=")) {
                        serviceNamespace = attr.split("=")[1];
                        break;
                    }
                }
            }
            if (serviceNamespace == null) {
                serviceNamespace = "example"; // default value
            }
        }

        // 2. Configure OTLP exporter
        OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(endpoint)
                .setTimeout(5, TimeUnit.SECONDS)
                .build();

        // 3. Set up resource attributes
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.builder()
                        .put("service.name", serviceName)
                        .put("service.namespace", serviceNamespace)
                        .build()));

        // 4. Create meter provider with periodic export
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(
                        PeriodicMetricReader.builder(metricExporter)
                                .setInterval(5, TimeUnit.SECONDS)
                                .build())
                .build();

        // 5. Initialize OpenTelemetry
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal();

        // 6. Get meter and create counter
        Meter meter = openTelemetry.getMeter("my-meter");
        LongCounter counter = meter.counterBuilder("app.operations.count")
                .setDescription("Total number of operations")
                .setUnit("1")
                .build();

        // 7. Create attributes
        Attributes attributes = Attributes.builder()
                .put("deployment.environment", "test")
                .build();

        // 8. Simulate metric generation
        int count = 0;
        while (true) {
            counter.add(1, attributes);
            System.out.println("Sent metric update: " + ++count);
            TimeUnit.SECONDS.sleep(5);
        }
    }
}
