package com.chefpro.backendjava.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationSchemaMigration implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;

  public ReservationSchemaMigration(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    ensurePaymentStatusColumn();
    ensureCancellationReasonColumn();
  }

  private void ensurePaymentStatusColumn() {
    Integer count = jdbcTemplate.queryForObject(
      """
      SELECT COUNT(*)
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'reservations'
        AND column_name = 'payment_status'
      """,
      Integer.class
    );

    if (count != null && count == 0) {
      jdbcTemplate.execute(
        "ALTER TABLE reservations ADD COLUMN payment_status ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING'"
      );
    }
  }

  private void ensureCancellationReasonColumn() {
    Integer count = jdbcTemplate.queryForObject(
      """
      SELECT COUNT(*)
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 'reservations'
        AND column_name = 'cancellation_reason'
      """,
      Integer.class
    );

    if (count != null && count == 0) {
      jdbcTemplate.execute(
        "ALTER TABLE reservations ADD COLUMN cancellation_reason VARCHAR(100) NULL"
      );
    }
  }
}
