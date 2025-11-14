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
}
