package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.ReleaseInputView;
import org.example.view.ReleaseOutputView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseControllerTest {

    private record Context(ReleaseController controller,
                           InMemoryOrderRepository orderRepo,
                           InMemorySampleRepository sampleRepo,
                           ByteArrayOutputStream output) {}

    private Context buildContext(String input,
                                 InMemoryOrderRepository orderRepo,
                                 InMemorySampleRepository sampleRepo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ReleaseInputView inputView = new ReleaseInputView(scanner);
        ReleaseOutputView outputView = new ReleaseOutputView(
                new PrintStream(baos, true, StandardCharsets.UTF_8));
        return new Context(
                new ReleaseController(sampleRepo, orderRepo, inputView, outputView),
                orderRepo, sampleRepo, baos);
    }

    @Test
    @DisplayName("CONFIRMED 주문이 없을 때 안내 메시지를 출력하고 복귀한다")
    void run_withNoConfirmedOrders_showsEmptyMessage() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.RESERVED));

        Context ctx = buildContext("", orderRepo, new InMemorySampleRepository());
        ctx.controller().run();

        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("없"));
    }

    @Test
    @DisplayName("유효한 번호 입력 시 주문 상태가 RELEASE로 전환된다")
    void run_withValidNumber_releasesOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "웨이퍼A", 0.5, 0.9, 280));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.CONFIRMED));

        Context ctx = buildContext("1\n", orderRepo, sampleRepo);
        ctx.controller().run();

        assertEquals(OrderStatus.RELEASE, orderRepo.findById("ORD-001").get().getStatus());
    }

    @Test
    @DisplayName("0 입력 시 주문 상태가 변경되지 않는다")
    void run_withCancelInput_doesNotChangeStatus() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "웨이퍼A", 0.5, 0.9, 280));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.CONFIRMED));

        Context ctx = buildContext("0\n", orderRepo, sampleRepo);
        ctx.controller().run();

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("ORD-001").get().getStatus());
    }

    @Test
    @DisplayName("PRODUCING 경로 출고 시 주문 수량만큼 재고가 차감된다")
    void run_producingPathRelease_deductsStockByOrderQuantity() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        // 생산 완료 후 재고: 30(기존) + 210(생산) = 240
        sampleRepo.save(new Sample("S-001", "웨이퍼A", 0.5, 0.9, 240));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        Order order = new Order("ORD-001", "S-001", "고객A", 200, OrderStatus.CONFIRMED);
        order.setActualQty(210); // PRODUCING 경로 표시
        orderRepo.save(order);

        Context ctx = buildContext("1\n", orderRepo, sampleRepo);
        ctx.controller().run();

        assertEquals(40, sampleRepo.findById("S-001").get().getStock(),
                "출고 후 재고 = 240 - 200 = 40");
    }

    @Test
    @DisplayName("CONFIRMED 직접 경로 출고 시 재고가 변경되지 않는다 (승인 시 이미 차감됨)")
    void run_confirmedDirectPathRelease_doesNotChangeStock() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        // 승인 시 이미 차감된 재고: 480 - 200 = 280
        sampleRepo.save(new Sample("S-001", "웨이퍼A", 0.5, 0.9, 280));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        Order order = new Order("ORD-001", "S-001", "고객A", 200, OrderStatus.CONFIRMED);
        // actualQty = 0 (default) — CONFIRMED 직접 경로
        orderRepo.save(order);

        Context ctx = buildContext("1\n", orderRepo, sampleRepo);
        ctx.controller().run();

        assertEquals(280, sampleRepo.findById("S-001").get().getStock(),
                "CONFIRMED 직접 경로는 승인 시 차감됐으므로 출고 시 재고 변화 없음");
    }
}
