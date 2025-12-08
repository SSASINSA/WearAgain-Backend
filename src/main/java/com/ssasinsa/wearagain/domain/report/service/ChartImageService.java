package com.ssasinsa.wearagain.domain.report.service;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChartImageService {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://quickchart.io")
            .build();

    public String generateLineChartBase64(List<String> labels, List<? extends Number> values) {
        if (labels == null || values == null || labels.isEmpty() || labels.size() != values.size()) {
            return null;
        }
        String config = buildLineConfig(labels, values);
        try {
            byte[] image = webClient.post()
                    .uri("/chart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("chart", config))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(TIMEOUT)
                    .onErrorResume(ex -> Mono.empty())
                    .blockOptional()
                    .orElse(null);
            return image == null ? null : Base64.getEncoder().encodeToString(image);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String generateMultiLineChartBase64(List<String> labels, Map<String, List<? extends Number>> datasets) {
        if (labels == null || datasets == null || labels.isEmpty() || datasets.isEmpty()) {
            return null;
        }
        String config = buildMultiLineConfig(labels, datasets);
        try {
            byte[] image = webClient.post()
                    .uri("/chart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("chart", config))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(TIMEOUT)
                    .onErrorResume(ex -> Mono.empty())
                    .blockOptional()
                    .orElse(null);
            return image == null ? null : Base64.getEncoder().encodeToString(image);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildLineConfig(List<String> labels, List<? extends Number> values) {
        StringBuilder labelsArray = new StringBuilder();
        labelsArray.append("[");
        for (int i = 0; i < labels.size(); i++) {
            labelsArray.append("\"").append(labels.get(i)).append("\"");
            if (i < labels.size() - 1) {
                labelsArray.append(",");
            }
        }
        labelsArray.append("]");

        StringBuilder dataArray = new StringBuilder();
        dataArray.append("[");
        for (int i = 0; i < values.size(); i++) {
            dataArray.append(values.get(i));
            if (i < values.size() - 1) {
                dataArray.append(",");
            }
        }
        dataArray.append("]");

        return """
                {
                  type: 'line',
                  data: {
                    labels: %s,
                    datasets: [{
                      label: '체크인 수',
                      data: %s,
                      borderColor: '#007bff',
                      backgroundColor: 'rgba(0, 123, 255, 0.15)',
                      fill: true,
                      tension: 0.25
                    }]
                  },
                  options: {
                    responsive: false,
                    plugins: { legend: { display: false } },
                    scales: {
                      x: { ticks: { color: '#495057' } },
                      y: { ticks: { color: '#495057' }, beginAtZero: true }
                    }
                  }
                }
                """.formatted(labelsArray, dataArray);
    }

    private String buildMultiLineConfig(List<String> labels, Map<String, List<? extends Number>> datasets) {
        String labelsJson = toStringArray(labels);
        StringBuilder datasetJson = new StringBuilder();
        datasetJson.append("[");
        int idx = 0;
        for (Map.Entry<String, List<? extends Number>> entry : datasets.entrySet()) {
            String label = entry.getKey();
            List<? extends Number> values = entry.getValue();
            if (values == null || values.size() != labels.size()) {
                continue;
            }
            if (idx > 0) {
                datasetJson.append(",");
            }
            datasetJson.append("{");
            datasetJson.append("label:\"").append(label).append("\",");
            datasetJson.append("data:").append(toNumberArray(values)).append(",");
            datasetJson.append("borderColor:\"").append(pickColor(idx)).append("\",");
            datasetJson.append("backgroundColor:\"").append(pickColor(idx)).append("22\",");
            datasetJson.append("fill:false,");
            datasetJson.append("tension:0.25");
            datasetJson.append("}");
            idx++;
        }
        datasetJson.append("]");

        return """
                {
                  type: 'line',
                  data: {
                    labels: %s,
                    datasets: %s
                  },
                  options: {
                    responsive: false,
                    plugins: { legend: { display: true, position: 'bottom' } },
                    scales: {
                      x: { ticks: { color: '#495057' } },
                      y: { ticks: { color: '#495057' }, beginAtZero: true, suggestedMax: 10 }
                    }
                  }
                }
                """.formatted(labelsJson, datasetJson);
    }

    private String toStringArray(List<String> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < items.size(); i++) {
            builder.append("\"").append(items.get(i)).append("\"");
            if (i < items.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private String toNumberArray(List<? extends Number> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i));
            if (i < items.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private String pickColor(int index) {
        String[] palette = {
                "#007bff", "#28a745", "#dc3545", "#ffc107", "#17a2b8",
                "#6f42c1", "#20c997", "#fd7e14", "#6610f2", "#e83e8c"
        };
        return palette[index % palette.length];
    }
}
