package com.ssasinsa.wearagain.domain.user.docs;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserExamples {

    public static final String USER_SUMMARY_RESPONSE = """
            {
              \"displayName\": \"김웨어\",
              \"ticketBalance\": 3,
              \"creditBalance\": 120,
              \"totalTicketChangeAmount\": 15
            }
            """;
}
