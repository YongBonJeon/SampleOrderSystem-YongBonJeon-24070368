package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.ApprovalInputView;
import org.example.view.ApprovalOutputView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalControllerTest {

    private record Context(ApprovalController controller,
                           InMemoryOrderRepository orderRepo,
                           InMemorySampleRepository sampleRepo,
                           ProductionQueue queue,
                           ByteArrayOutputStream output) {}

    private Context buildContext(String input,
                                 InMemorySampleRepository sampleRepo,
                                 InMemoryOrderRepository orderRepo,
                                 ProductionQueue queue) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ApprovalInputView inputView = new ApprovalInputView(scanner);
        ApprovalOutputView outputView = new ApprovalOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8));
        return new Context(
                new ApprovalController(sampleRepo, orderRepo, queue, inputView, outputView),
                orderRepo, sampleRepo, queue, baos);
    }

    @Test
    @DisplayName("번호 선택 후 재고가 충분할 때 재고 현황이 출력된다")
    void run_showsStockInfo_whenStockSufficient() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "산화막 웨이퍼-SiO2", 0.5, 0.9, 480));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "1\nY\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("480"), "현재 재고");
        assertTrue(output.contains("200"), "주문 수량");
        assertTrue(output.contains("충분"), "재고 상태");
    }

    @Test
    @DisplayName("번호 선택 후 재고가 부족할 때 부족 현황이 출력된다")
    void run_showsStockInfo_whenStockInsufficient() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "산화막 웨이퍼-SiO2", 0.5, 0.9, 30));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "1\nY\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("30"), "현재 재고");
        assertTrue(output.contains("200"), "주문 수량");
        assertTrue(output.contains("부족"), "재고 상태");
    }

    @Test
    @DisplayName("번호 입력으로 주문을 선택해 거절할 수 있다")
    void run_selectByNumber_rejectsSelectedOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "산화막 웨이퍼-SiO2", 0.5, 0.9, 480));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "1\nN\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        assertEquals(OrderStatus.REJECTED, orderRepo.findById("ORD-20260612-0001").get().getStatus());
    }

    @Test
    @DisplayName("0 입력 시 취소하고 주문 상태가 변경되지 않는다")
    void run_cancelWithZero_doesNotChangeOrderStatus() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "산화막 웨이퍼-SiO2", 0.5, 0.9, 480));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "0\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        assertEquals(OrderStatus.RESERVED, orderRepo.findById("ORD-20260612-0001").get().getStatus());
    }

    @Test
    @DisplayName("예약 목록에 시료명이 포함된다")
    void run_showsReservedList_includesSampleName() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "산화막 웨이퍼-SiO2", 0.5, 0.9, 480));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "0\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("산화막 웨이퍼-SiO2"));
    }

    @Test
    @DisplayName("대기 주문이 없을 때 안내 메시지를 출력한다")
    void run_withNoReservedOrders_showsEmptyMessage() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        Context ctx = buildContext("", sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        String output = ctx.output().toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("없"));
    }

    @Test
    @DisplayName("거절 입력 시 주문이 REJECTED로 전환된다")
    void reject_order_rejectsOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 480));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "1\nN\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        assertEquals(OrderStatus.REJECTED, orderRepo.findById("ORD-20260612-0001").get().getStatus());
    }

    @Test
    @DisplayName("재고가 부족할 때 승인하면 PRODUCING으로 전환되고 생산 큐에 등록된다")
    void approve_withInsufficientStock_producesOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 30));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        ProductionQueue queue = new ProductionQueue();
        String input = "1\nY\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, queue);

        ctx.controller().run();

        Order order = orderRepo.findById("ORD-20260612-0001").get();
        assertEquals(OrderStatus.PRODUCING, order.getStatus());
        assertFalse(queue.isEmpty());
        assertEquals("ORD-20260612-0001", queue.peek().get().getOrderId());
    }

    @Test
    @DisplayName("재고가 충분할 때 승인하면 CONFIRMED로 전환되고 재고가 차감된다")
    void approve_withSufficientStock_confirmsOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 480));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.RESERVED));

        String input = "1\nY\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        Order order = orderRepo.findById("ORD-20260612-0001").get();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(280, sampleRepo.findById("S-001").get().getStock());
    }
}
