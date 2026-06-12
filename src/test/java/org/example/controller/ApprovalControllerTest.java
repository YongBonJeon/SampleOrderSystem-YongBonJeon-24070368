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

        String input = "ORD-20260612-0001\nN\n";
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
        String input = "ORD-20260612-0001\nY\n";
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

        // 주문번호 입력, Y(승인)
        String input = "ORD-20260612-0001\nY\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo, new ProductionQueue());

        ctx.controller().run();

        Order order = orderRepo.findById("ORD-20260612-0001").get();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(280, sampleRepo.findById("S-001").get().getStock());
    }
}
