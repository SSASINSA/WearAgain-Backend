package com.ssasinsa.wearagain.domain.report.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "report.storage")
public class ReportStorageProperties {

    /**
     * 리포트 PDF 파일을 저장할 로컬 경로 (예: /var/wearagain/reports)
     */
    private String outputDir;

    /**
     * 저장된 리포트 접근을 위한 베이스 URL (예: https://static.wearagain.kr/reports)
     */

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
