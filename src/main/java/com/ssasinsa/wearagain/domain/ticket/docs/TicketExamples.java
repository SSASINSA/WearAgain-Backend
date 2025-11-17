package com.ssasinsa.wearagain.domain.ticket.docs;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TicketExamples {

    public static final String USER_TICKET_QR_RESPONSE = """
            {
              \"ticketCount\": 5,
              \"ticketToken\": \"ae9e35d24f574c9b8a5b6b1d45e285ec\",
              \"ticketTokenExpiresIn\": 900
            }
            """;

    public static final String STAFF_TICKET_USE_REQUEST = """
            {
              \"qrToken\": \"ae9e35d24f574c9b8a5b6b1d45e285ec\",
              \"code\": \"023941\",
              \"amount\": 1
            }
            """;

    public static final String STAFF_TICKET_USE_RESPONSE = """
            {
              \"ticketCountBefore\": 3,
              \"ticketCountAfter\": 2,
              \"checkedInAt\": \"2025-02-10T09:05:12Z\"
            }
            """;

    public static final String STAFF_TICKET_CHARGE_REQUEST = """
            {
              \"qrToken\": \"ae9e35d24f574c9b8a5b6b1d45e285ec\",
              \"code\": \"023941\",
              \"amount\": 3
            }
            """;

    public static final String STAFF_TICKET_CHARGE_RESPONSE = """
            {
              \"ticketCountBefore\": 2,
              \"ticketCountAfter\": 5,
              \"chargedAt\": \"2025-02-11T10:00:00Z\"
            }
            """;

    public static final String ADMIN_TICKET_CHARGE_REQUEST = """
            {
              \"userId\": 1,
              \"amount\": 5,
              \"reason\": \"이벤트 보상\"
            }
            """;

    public static final String ADMIN_TICKET_CHARGE_RESPONSE = """
            {
              \"ticketCountBefore\": 2,
              \"ticketCountAfter\": 7,
              \"chargedAt\": \"2025-02-11T10:00:00Z\"
            }
            """;
}
