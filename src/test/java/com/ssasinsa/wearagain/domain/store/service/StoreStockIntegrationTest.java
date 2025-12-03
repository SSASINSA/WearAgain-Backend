package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreOrderCreateRequest;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrder;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreOrderRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StoreStockIntegrationTest {

    @Autowired
    private StoreService storeService;
    @Autowired
    private StoreItemRepository storeItemRepository;
    @Autowired
    private StoreOrderRepository storeOrderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CreditHistoryRepository creditHistoryRepository;
    @Autowired
    private StoreStockService storeStockService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private StoreItemImageRepository storeItemImageRepository;

    private User user;
    private StoreItem item;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

        user = User.create("buyer@test.com", "구매자", null);
        user.updateCreditBalance(10_000);
        user = userRepository.save(user);

        item = StoreItem.create(
                "테스트 상품",
                "설명",
                "카테고리",
                1000,
                5,
                5,
                StoreItemStatus.ACTIVE,
                List.of(),
                List.of("강남 팝업스토어")
        );
        item = storeItemRepository.save(item);
        storeStockService.reset(item.getId(), item.getStock());
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @Transactional
    void 구매와_취소시_redis와_db재고가_일관된다() {
        // 주문 생성
        StoreOrderCreateRequest createRequest = new StoreOrderCreateRequest(item.getId(), 2, "강남 팝업스토어");
        var response = storeService.createOrder(createRequest, user.getId());

        StoreOrder order = storeOrderRepository.findById(response.orderId()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(StoreOrderStatus.PURCHASED);
        assertThat(fetchRedisStock()).isEqualTo(3);
        assertThat(storeItemRepository.findById(item.getId()).orElseThrow().getStock()).isEqualTo(3);

        // 주문 취소
        storeService.cancelOrder(order.getId(), user.getId());

        StoreOrder canceled = storeOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(StoreOrderStatus.CANCELED);
        assertThat(fetchRedisStock()).isEqualTo(5);
        assertThat(storeItemRepository.findById(item.getId()).orElseThrow().getStock()).isEqualTo(5);
    }

    private int fetchRedisStock() {
        String value = redisTemplate.opsForValue().get("store:stock:item:" + item.getId());
        return value == null ? 0 : Integer.parseInt(value);
    }
}
