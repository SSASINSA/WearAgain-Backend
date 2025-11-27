package com.ssasinsa.wearagain.domain.store.docs;

public final class StoreExamples {

    private StoreExamples() {
    }

    public static final String ADMIN_STORE_ITEM_CREATE_REQUEST = """
            {
              "name": "웰컴 티셔츠",
              "description": "유기농 코튼",
              "category": "top",
              "price": 1200,
              "stock": 25,
              "maxPurchasePerUser": 1,
              "status": "ACTIVE",
              "pickupLocations": ["강남 팝업스토어", "홍대 매장"],
              "images": [
                { "imageUrl": "https://cdn.../main.jpg", "sortOrder": 1 },
                { "imageUrl": "https://cdn.../sub.jpg", "sortOrder": 2 }
              ]
            }
            """;

    public static final String ADMIN_STORE_ITEM_CREATE_RESPONSE = """
            {
              "id": 101,
              "name": "웰컴 티셔츠",
              "status": "ACTIVE"
            }
            """;

    public static final String ADMIN_STORE_ITEM_UPDATE_REQUEST = """
            {
              "name": "웰컴 티셔츠 리미티드",
              "description": "유기농 코튼, 한정판",
              "price": 1500,
              "stock": 30,
              "maxPurchasePerUser": 2,
              "status": "ACTIVE",
              "pickupLocations": ["강남 팝업스토어"],
              "images": [
                { "imageUrl": "https://cdn.../main.jpg", "sortOrder": 1 }
              ]
            }
            """;

    public static final String ADMIN_STORE_ITEM_UPDATE_RESPONSE = """
            {
              "id": 101,
              "name": "웰컴 티셔츠 리미티드",
              "description": "유기농 코튼, 한정판",
              "category": "top",
              "price": 1500,
              "stock": 30,
              "maxPurchasePerUser": 2,
              "status": "ACTIVE",
              "pickupLocations": ["강남 팝업스토어"],
              "images": [
                { "id": 10, "imageUrl": "https://cdn.../main.jpg", "sortOrder": 1 }
              ],
              "createdAt": "2025-02-11T03:12:00Z",
              "updatedAt": "2025-02-12T01:00:00Z"
            }
            """;

    public static final String USER_STORE_ORDER_CREATE_REQUEST = """
            {
              "itemId": 101,
              "quantity": 2,
              "pickupLocation": "강남 팝업스토어"
            }
            """;

    public static final String USER_STORE_ORDER_CREATE_RESPONSE = """
            {
              "orderId": 501,
              "itemId": 101,
              "itemName": "웰컴 티셔츠",
              "quantity": 2,
              "unitPrice": 1200,
              "usedCredit": 2400,
              "pickupLocation": "강남 팝업스토어",
              "status": "PURCHASED",
              "purchasedAt": "2025-02-11T04:00:00Z"
            }
            """;

    public static final String USER_STORE_ORDER_CANCEL_RESPONSE = """
            {
              "orderId": 501,
              "status": "CANCELED",
              "refundedCredit": 2400,
              "canceledAt": "2025-02-11T04:10:00Z"
            }
            """;
}
