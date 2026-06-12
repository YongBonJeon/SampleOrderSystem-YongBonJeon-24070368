package org.example.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderIdGeneratorTest {

    @Test
    @DisplayName("첫 번째 호출 시 ORD-날짜-0001 형식을 반환한다")
    void generate_firstCall_returnsFormattedId() {
        OrderIdGenerator generator = new OrderIdGenerator(LocalDate.of(2026, 6, 12));

        String orderId = generator.generate();

        assertEquals("ORD-20260612-0001", orderId);
    }

    @Test
    @DisplayName("같은 날 두 번째 호출 시 순번이 0002로 증가한다")
    void generate_secondCall_incrementsSequence() {
        OrderIdGenerator generator = new OrderIdGenerator(LocalDate.of(2026, 6, 12));
        generator.generate();

        String orderId = generator.generate();

        assertEquals("ORD-20260612-0002", orderId);
    }

    @Test
    @DisplayName("기존 주문이 있을 때 다음 시퀀스 번호를 이어받는다")
    void resumesSequenceFromExistingOrders() {
        List<String> existingIds = List.of("ORD-20260612-0001", "ORD-20260612-0003");
        OrderIdGenerator generator = new OrderIdGenerator(LocalDate.of(2026, 6, 12), existingIds);

        String orderId = generator.generate();

        assertEquals("ORD-20260612-0004", orderId);
    }

    @Test
    @DisplayName("날짜가 바뀌면 순번이 0001로 리셋된다")
    void generate_newDate_resetsSequence() {
        OrderIdGenerator generatorDay1 = new OrderIdGenerator(LocalDate.of(2026, 6, 12));
        generatorDay1.generate();
        OrderIdGenerator generatorDay2 = new OrderIdGenerator(LocalDate.of(2026, 6, 13));

        String orderId = generatorDay2.generate();

        assertEquals("ORD-20260613-0001", orderId);
    }
}
