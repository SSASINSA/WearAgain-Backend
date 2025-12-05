CREATE TABLE dashboard_snapshot (
    dashboard_snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    event_total_open_closed BIGINT NOT NULL,
    event_manager_hosted BIGINT NOT NULL,
    participants_checked_in BIGINT NOT NULL,
    tickets_charged BIGINT NOT NULL,
    tickets_used BIGINT NOT NULL,
    exchange_rate DECIMAL(19,4) NOT NULL,
    impact_co2_saved DECIMAL(19,4) NOT NULL,
    impact_water_saved DECIMAL(19,4) NOT NULL,
    impact_energy_saved DECIMAL(19,4) NOT NULL
);
