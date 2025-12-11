package com.ssasinsa.wearagain.domain.report.service;

import com.ssasinsa.wearagain.domain.report.ReportStatus;
import com.ssasinsa.wearagain.domain.report.config.ReportStorageProperties;
import com.ssasinsa.wearagain.domain.report.dto.ReportCreateResponse;
import com.ssasinsa.wearagain.domain.report.dto.ReportStatusResponse;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EventReportService {

    private final ReportStorageProperties storageProperties;
    private final TemplateEngine templateEngine;
    private final EventRepository eventRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final ChartImageService chartImageService;
    private final ImpactAnalyticsRepository impactAnalyticsRepository;

    private final Map<Long, ReportStatusResponse> reportStore = new ConcurrentHashMap<>();

    public ReportCreateResponse requestReport(Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId must not be null");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        String reportId = "event-" + eventId;
        ensureOutputDir();

        String fileName = formatReportFileName(event);
        Path outputPath = Path.of(storageProperties.getOutputDir(), fileName);

        // 이미 생성된 파일이 있으면 바로 READY로 응답
        if (Files.exists(outputPath)) {
            String downloadUrl = buildDownloadUrl(fileName);
            ReportStatusResponse ready = new ReportStatusResponse(reportId, ReportStatus.READY, downloadUrl, null);
            reportStore.put(eventId, ready);
            return new ReportCreateResponse(reportId, ready.status(), ready.downloadUrl());
        }

        ReportStatusResponse pending = new ReportStatusResponse(reportId, ReportStatus.PENDING, null, null);
        reportStore.put(eventId, pending);

        try {
            String html = renderTemplate(buildContext(event));
            renderPdf(html, outputPath);
            String downloadUrl = buildDownloadUrl(fileName);
            ReportStatusResponse ready = new ReportStatusResponse(reportId, ReportStatus.READY, downloadUrl, null);
            reportStore.put(eventId, ready);
            return new ReportCreateResponse(reportId, ready.status(), ready.downloadUrl());
        } catch (Exception exception) {
            ReportStatusResponse failed = new ReportStatusResponse(
                    reportId,
                    ReportStatus.FAILED,
                    null,
                    exception.getMessage()
            );
            reportStore.put(eventId, failed);
            return new ReportCreateResponse(reportId, failed.status(), failed.downloadUrl());
        }
    }

    public ReportStatusResponse getReportStatus(String reportId) {
        Long eventId = parseReportId(reportId);
        if (eventId == null) {
            return new ReportStatusResponse(reportId, ReportStatus.FAILED, null, "Report not found");
        }
        ReportStatusResponse status = reportStore.get(eventId);
        if (status != null) {
            return status;
        }
        return buildReadyResponseIfFileExists(eventId, reportId)
                .orElseGet(() -> new ReportStatusResponse(reportId, ReportStatus.FAILED, null, "Report not found"));
    }

    private Context buildContext(Event event) {
        long totalApplied = eventApplicationRepository.countByEvent_IdAndStatusIn(
                event.getId(),
                EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN)
        );
        long checkedIn = eventApplicationRepository.countByEvent_IdAndStatusIn(
                event.getId(),
                EnumSet.of(EventApplicationStatus.CHECKED_IN)
        );
        long noShow = Math.max(0, totalApplied - checkedIn);
        double attendanceRate = totalApplied == 0 ? 0 : (double) checkedIn / totalApplied;
        double noShowRate = totalApplied == 0 ? 0 : (double) noShow / totalApplied;

        long donated = ticketHistoryRepository.sumPositiveAmountsByEventIds(java.util.List.of(event.getId())).stream()
                .findFirst()
                .map(com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryEventSum::amount)
                .orElse(0L);
        long exchanged = ticketHistoryRepository.sumNegativeAmountsAbsByEventIds(java.util.List.of(event.getId())).stream()
                .findFirst()
                .map(com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryEventSum::amount)
                .orElse(0L);
        double exchangeRate = donated == 0 ? 0 : (double) exchanged / donated;

        var impactSummary = impactAnalyticsRepository.aggregateByEventId(event.getId());
        BigDecimal co2Saved = impactSummary == null ? BigDecimal.ZERO : impactSummary.co2Saved();
        BigDecimal waterSaved = impactSummary == null ? BigDecimal.ZERO : impactSummary.waterSaved();
        BigDecimal energySaved = impactSummary == null ? BigDecimal.ZERO : impactSummary.energySaved();
        String co2Note = buildPerItemNote(co2Saved, donated, "kg");
        String waterNote = buildPerItemNote(waterSaved, donated, "L");
        String energyNote = buildPerItemNote(energySaved, donated, "kWh");

        Context context = new Context();
        context.setVariable("event", Map.of(
                "title", event.getTitle(),
                "documentId", "EVT-" + event.getId(),
                "startDate", event.getStartDate(),
                "endDate", event.getEndDate(),
                "location", event.getLocation(),
                "managerId", event.getOrganizerAdmin() != null ? event.getOrganizerAdmin().getId() : null,
                "status", event.getStatus().name()
        ));
        context.setVariable("stats", Map.of(
                "totalApplied", totalApplied,
                "checkedIn", checkedIn,
                "attendanceRate", attendanceRate,
                "noShowCount", noShow,
                "noShowRate", noShowRate,
                "donatedClothes", donated,
                "exchangedClothes", exchanged,
                "exchangeRate", exchangeRate
        ));
        context.setVariable("impact", Map.of(
                "co2Saved", co2Saved,
                "co2Note", co2Note,
                "waterSaved", waterSaved,
                "waterNote", waterNote,
                "energySaved", energySaved,
                "energyNote", energyNote
        ));
        context.setVariable("chartImage", generateCheckinChart(event.getId()));
        context.setVariable("ticketChartImage", generateTicketChart(event.getId()));
        return context;
    }

    private String generateCheckinChart(Long eventId) {
        List<LocalDateTime> checkinTimes = eventApplicationRepository.findCheckedInTimesByEventId(eventId);
        if (checkinTimes == null || checkinTimes.isEmpty()) {
            return null;
        }

        List<String> hourLabels = buildHourLabels();
        Map<String, List<? extends Number>> datasets = aggregateHourlyByDate(checkinTimes, hourLabels.size());
        if (datasets.isEmpty()) {
            return null;
        }
        ChartData trimmed = trimChartData(hourLabels, datasets);
        if (trimmed == null) {
            return null;
        }
        return chartImageService.generateMultiLineChartBase64(trimmed.labels(), trimmed.datasets());
    }

    private String generateTicketChart(Long eventId) {
        List<com.ssasinsa.wearagain.domain.finance.entity.TicketHistory> histories =
                ticketHistoryRepository.findByRelatedEventId(eventId);
        if (histories == null || histories.isEmpty()) {
            return null;
        }
        List<String> hourLabels = buildHourLabels();
        int size = hourLabels.size();
        int[] charge = new int[size];
        int[] use = new int[size];
        for (var history : histories) {
            if (history.getCreatedAt() == null) {
                continue;
            }
            int hour = history.getCreatedAt().getHour();
            if (hour < 0 || hour >= size) {
                continue;
            }
            int amount = history.getChangeAmount();
            if (amount > 0) {
                charge[hour] += amount;
            } else if (amount < 0) {
                use[hour] += Math.abs(amount);
            }
        }
        Map<String, List<? extends Number>> datasets = new LinkedHashMap<>();
        datasets.put("충전", java.util.Arrays.stream(charge).boxed().toList());
        datasets.put("사용", java.util.Arrays.stream(use).boxed().toList());
        ChartData trimmed = trimChartData(hourLabels, datasets);
        if (trimmed == null) {
            return null;
        }
        return chartImageService.generateMultiLineChartBase64(trimmed.labels(), trimmed.datasets());
    }

    private Map<String, List<? extends Number>> aggregateHourlyByDate(List<LocalDateTime> checkinTimes, int hoursPerDay) {
        Map<LocalDate, List<LocalDateTime>> groupedByDate = checkinTimes.stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate));

        Map<String, List<? extends Number>> result = new LinkedHashMap<>();
        for (LocalDate date : groupedByDate.keySet().stream().sorted().toList()) {
            int[] counts = new int[hoursPerDay];
            for (LocalDateTime time : groupedByDate.get(date)) {
                int hour = time.getHour();
                if (hour >= 0 && hour < hoursPerDay) {
                    counts[hour]++;
                }
            }
            List<Integer> hourCounts = java.util.Arrays.stream(counts).boxed().toList();
            result.put(date.toString(), hourCounts);
        }
        return result;
    }

    private List<String> buildHourLabels() {
        return java.util.stream.IntStream.range(0, 24)
                .mapToObj(h -> String.format("%02d:00", h))
                .toList();
    }

    private ChartData trimChartData(List<String> labels, Map<String, List<? extends Number>> datasets) {
        int min = labels.size();
        int max = -1;
        for (List<? extends Number> values : datasets.values()) {
            for (int i = 0; i < values.size(); i++) {
                Number v = values.get(i);
                if (v != null && v.doubleValue() > 0) {
                    min = Math.min(min, i);
                    max = Math.max(max, i);
                }
            }
        }
        if (max < 0) {
            return null; // all zero
        }
        int from = Math.max(0, min - 1);
        int to = Math.min(labels.size() - 1, max + 1);
        List<String> slicedLabels = labels.subList(from, to + 1);
        Map<String, List<? extends Number>> slicedDatasets = new LinkedHashMap<>();
        for (Map.Entry<String, List<? extends Number>> entry : datasets.entrySet()) {
            List<? extends Number> vals = entry.getValue();
            if (vals == null || vals.size() <= to) {
                continue;
            }
            List<Number> slice = new java.util.ArrayList<>(vals.subList(from, to + 1));
            if (!slice.isEmpty()) {
                slice.set(0, 0);
                slice.set(slice.size() - 1, 0);
            }
            slicedDatasets.put(entry.getKey(), slice);
        }
        if (slicedDatasets.isEmpty()) {
            return null;
        }
        return new ChartData(slicedLabels, slicedDatasets);
    }

    private String renderTemplate(Context context) {
        return templateEngine.process("report-example", context);
    }

    private void renderPdf(String html, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        try (var outputStream = Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // fastMode는 폰트/레이아웃 이슈를 유발할 수 있어 한글 검증 시 비활성화
            builder.withHtmlContent(html, "file:///");
            registerFont(builder);
            builder.defaultTextDirection(BaseRendererBuilder.TextDirection.LTR);
            builder.toStream(outputStream);
            builder.run();
        } catch (Exception exception) {
            throw new IOException("PDF 생성 중 오류가 발생했습니다.", exception);
        }
    }

    private void registerFont(PdfRendererBuilder builder) throws IOException {
        ClassPathResource fontResource = new ClassPathResource("fonts/NanumBarunGothic.ttf");
        if (!fontResource.exists()) {
            return;
        }
        Path fontPath = resolveFontPath(fontResource);
        builder.useFont(fontPath.toFile(), "NanumBarunGothic", 400, BaseRendererBuilder.FontStyle.NORMAL, true);
    }

    private Path resolveFontPath(ClassPathResource fontResource) throws IOException {
        try {
            return fontResource.getFile().toPath();
        } catch (IOException exception) {
            Path tempFile = Files.createTempFile("NanumBarunGothic", ".ttf");
            try (InputStream input = fontResource.getInputStream()) {
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            tempFile.toFile().deleteOnExit();
            return tempFile;
        }
    }

    private String buildDownloadUrl(String fileName) {
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String baseUrl = StringUtils.hasText(storageProperties.getBaseUrl())
                ? storageProperties.getBaseUrl()
                : "https://ssasinsa.co.kr/reports";
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBase + "/" + encodedName;
    }

    private void ensureOutputDir() {
        String outputDir = storageProperties.getOutputDir();
        if (!StringUtils.hasText(outputDir)) {
            return;
        }
        try {
            Files.createDirectories(Path.of(outputDir));
        } catch (IOException ignored) {
        }
    }

    private String buildPerItemNote(BigDecimal total, long donated, String unit) {
        if (donated > 0 && total != null) {
            BigDecimal perItem = total.divide(BigDecimal.valueOf(donated), 2, RoundingMode.HALF_UP);
            return "총 절감량 " + total.stripTrailingZeros().toPlainString() + " " + unit
                    + ", 1벌당 약 " + perItem.stripTrailingZeros().toPlainString() + " " + unit;
        }
        return "집계 데이터 없음";
    }

    private record ChartData(List<String> labels, Map<String, List<? extends Number>> datasets) {
    }

    private String formatReportFileName(Event event) {
        String title = event.getTitle() == null ? "event" : event.getTitle();
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFC);
        // OS에서 문제가 되는 문자만 치환하고 한글/공백은 허용
        String sanitized = normalized.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (!StringUtils.hasText(sanitized)) {
            sanitized = "event";
        }
        return sanitized + "_" + event.getId() + ".pdf";
    }

    private Long parseReportId(String reportId) {
        if (!StringUtils.hasText(reportId)) {
            return null;
        }
        String trimmed = reportId.trim();
        if (trimmed.startsWith("event-")) {
            trimmed = trimmed.substring("event-".length());
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private java.util.Optional<ReportStatusResponse> buildReadyResponseIfFileExists(Long eventId, String reportId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    String fileName = formatReportFileName(event);
                    Path outputPath = Path.of(storageProperties.getOutputDir(), fileName);
                    if (Files.exists(outputPath)) {
                        String downloadUrl = buildDownloadUrl(fileName);
                        ReportStatusResponse ready = new ReportStatusResponse(reportId, ReportStatus.READY, downloadUrl, null);
                        reportStore.put(eventId, ready);
                        return ready;
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull);
    }
}
