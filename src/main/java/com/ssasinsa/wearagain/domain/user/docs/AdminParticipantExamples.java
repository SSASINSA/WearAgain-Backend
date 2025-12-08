package com.ssasinsa.wearagain.domain.user.docs;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AdminParticipantExamples {

    public static final String PARTICIPANT_LIST_RESPONSE = """
            {
              "content": [
                {
                  "participantId": 101,
                  "name": "김참가",
                  "email": "participant1@example.com",
                  "avatarUrl": "https://cdn.wearagain.kr/avatar/101.png",
                  "ticketBalance": 5,
                  "creditBalance": 1200,
                  "suspended": false,
                  "joinedAt": "2025-11-30T02:18:00Z"
                },
                {
                  "participantId": 205,
                  "name": "박플레이",
                  "email": "participant205@example.com",
                  "avatarUrl": "https://cdn.wearagain.kr/avatar/205.png",
                  "ticketBalance": 2,
                  "creditBalance": 800,
                  "suspended": true,
                  "joinedAt": "2025-09-12T07:45:00Z"
                }
              ],
              "totalElements": 240,
              "totalPages": 24,
              "size": 10,
              "number": 0,
              "hasNext": true,
              "hasPrevious": false,
              "summary": {
                "totalParticipants": 240,
                "totalTickets": 8120,
                "totalCredits": 532000,
                "participantsChangeFromLastMonth": 15,
                "ticketsChangeFromLastMonth": 320,
                "creditsChangeFromLastMonth": 8200
              }
            }
            """;

    public static final String PARTICIPANT_DETAIL_RESPONSE = """
            {
              "participantId": 101,
              "name": "김참가",
              "email": "participant1@example.com",
              "avatarUrl": "https://cdn.wearagain.kr/avatar/101.png",
              "ticketBalance": 5,
              "creditBalance": 1200,
              "suspended": false,
              "joinedAt": "2025-11-30T02:18:00Z",
              "updatedAt": "2025-12-04T09:05:12Z",
              "impact": {
                "co2Saved": 12.45,
                "waterSaved": 35.10,
                "energySaved": 4.80
              },
              "mascot": {
                "level": 3,
                "exp": 40,
                "nextLevelExp": 60,
                "magicScissorCount": 2,
                "cycles": 5
              },
              "recentEvents": [
                {
                  "eventId": 45,
                  "title": "12월 리사이클 플리마켓",
                  "thumbnailUrl": "https://cdn.wearagain.kr/events/45/main.jpg",
                  "status": "APPLIED",
                  "startDate": "2025-12-20",
                  "endDate": "2025-12-20",
                  "appliedAt": "2025-12-01T01:00:00Z"
                }
              ]
            }
            """;

    public static final String PARTICIPANT_STATS_RESPONSE = """
            {
              "totalParticipants": 240,
              "totalTickets": 8120,
              "totalCredits": 532000
            }
            """;

    public static final String SUSPENSION_REQUEST = """
            {
              "suspended": true
            }
            """;

    public static final String SUSPENSION_RESPONSE = """
            {
              "participantId": 205,
              "name": "박플레이",
              "email": "participant205@example.com",
              "avatarUrl": "https://cdn.wearagain.kr/avatar/205.png",
              "ticketBalance": 2,
              "creditBalance": 800,
              "suspended": true,
              "joinedAt": "2025-09-12T07:45:00Z",
              "updatedAt": "2025-12-05T00:15:12Z",
              "impact": {
                "co2Saved": 3.10,
                "waterSaved": 4.25,
                "energySaved": 1.05
              },
              "mascot": {
                "level": 2,
                "exp": 10,
                "nextLevelExp": 30,
                "magicScissorCount": 1,
                "cycles": 2
              },
              "recentEvents": []
            }
            """;

    public static final String PARTICIPANT_UPDATE_REQUEST = """
            {
              "ticketBalance": 10,
              "creditBalance": 2000
            }
            """;

    public static final String PARTICIPANT_UPDATE_RESPONSE = """
            {
              "participantId": 101,
              "name": "김참가",
              "email": "participant1@example.com",
              "avatarUrl": "https://cdn.wearagain.kr/avatar/101.png",
              "ticketBalance": 10,
              "creditBalance": 2000,
              "suspended": false,
              "joinedAt": "2025-11-30T02:18:00Z",
              "updatedAt": "2025-12-07T02:10:12Z",
              "impact": {
                "co2Saved": 12.45,
                "waterSaved": 35.10,
                "energySaved": 4.80
              },
              "mascot": {
                "level": 3,
                "exp": 40,
                "nextLevelExp": 60,
                "magicScissorCount": 2,
                "cycles": 5
              },
              "recentEvents": [
                {
                  "eventId": 45,
                  "title": "12월 리사이클 플리마켓",
                  "thumbnailUrl": "https://cdn.wearagain.kr/events/45/main.jpg",
                  "status": "APPLIED",
                  "startDate": "2025-12-20",
                  "endDate": "2025-12-20",
                  "appliedAt": "2025-12-01T01:00:00Z"
                }
              ]
            }
            """;

    public static final String ADMIN_USER_LIST_RESPONSE = """
            {
              "content": [
                {
                  "adminUserId": 1,
                  "email": "super@wearagain.kr",
                  "name": "최고 관리자",
                  "role": "SUPER_ADMIN",
                  "status": "ACTIVE",
                  "lastLoginAt": "2025-12-07T05:20:00Z",
                  "createdAt": "2025-11-01T02:00:00Z",
                  "updatedAt": "2025-12-05T09:00:00Z"
                },
                {
                  "adminUserId": 5,
                  "email": "event.admin@wearagain.kr",
                  "name": "행사 관리자",
                  "role": "ADMIN",
                  "status": "ACTIVE",
                  "lastLoginAt": "2025-11-25T10:00:00Z",
                  "createdAt": "2025-11-15T08:30:00Z",
                  "updatedAt": "2025-11-20T10:10:00Z"
                }
              ],
              "page": 0,
              "size": 20,
              "totalElements": 6,
              "totalPages": 1,
              "hasNext": false
            }
            """;
}
