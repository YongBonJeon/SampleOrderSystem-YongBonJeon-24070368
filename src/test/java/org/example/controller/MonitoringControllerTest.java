package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.MonitoringInputView;
import org.example.view.MonitoringOutputView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringControllerTest {

    private record Context(MonitoringController controller, ByteArrayOutputStream output) {}

    private Context buildContext(String input,
                                 InMemorySampleRepository sampleRepo,
                                 InMemoryOrderRepository orderRepo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        MonitoringInputView inputView = new MonitoringInputView(scanner);
        MonitoringOutputView outputView = new MonitoringOutputView(
                new PrintStream(baos, true, StandardCharsets.UTF_8));
        return new Context(new MonitoringController(sampleRepo, orderRepo, inputView, outputView), baos);
    }

    private String output(Context ctx) {
        return ctx.output().toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("[1] 선택 시 상태별 주문 건수를 출력하며 REJECTED는 제외된다")
    void showOrderCounts_excludesRejected() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED));
        orderRepo.save(new Order("ORD-002", "S-001", "고객B", 20, OrderStatus.RESERVED));
        orderRepo.save(new Order("ORD-003", "S-001", "고객C", 30, OrderStatus.PRODUCING));
        orderRepo.save(new Order("ORD-004", "S-001", "고객D", 40, OrderStatus.REJECTED));

        Context ctx = buildContext("1\n0\n", new InMemorySampleRepository(), orderRepo);
        ctx.controller().run();

        String out = output(ctx);
        assertTrue(out.contains("RESERVED"));
        assertTrue(out.contains("2건"));
        assertTrue(out.contains("PRODUCING"));
        assertTrue(out.contains("1건"));
        assertFalse(out.contains("REJECTED"));
    }

    @Test
    @DisplayName("[2] 선택 시 재고가 0인 시료는 고갈로 표시된다")
    void showStockStatus_withZeroStock_showsDepleted() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "산화막 웨이퍼-SiO2", 0.6, 0.88, 0));

        Context ctx = buildContext("2\n0\n", sampleRepo, new InMemoryOrderRepository());
        ctx.controller().run();

        assertTrue(output(ctx).contains("고갈"));
    }

    @Test
    @DisplayName("[2] 선택 시 RESERVED 주문 수량 합이 재고를 초과하면 부족으로 표시된다")
    void showStockStatus_withInsufficientStock_showsShortage() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "SiC 파워기판-6인치", 0.8, 0.92, 5));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED));

        Context ctx = buildContext("2\n0\n", sampleRepo, orderRepo);
        ctx.controller().run();

        assertTrue(output(ctx).contains("부족"));
    }

    @Test
    @DisplayName("[2] 선택 시 재고가 RESERVED 주문 수량 이상이면 여유로 표시된다")
    void showStockStatus_withSufficientStock_showsSurplus() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "실리콘 웨이퍼-8인치", 0.45, 0.95, 20));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 10, OrderStatus.RESERVED));

        Context ctx = buildContext("2\n0\n", sampleRepo, orderRepo);
        ctx.controller().run();

        assertTrue(output(ctx).contains("여유"));
    }
}
