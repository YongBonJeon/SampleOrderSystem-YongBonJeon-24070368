package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.Sample;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.util.OrderIdGenerator;
import org.example.view.OrderInputView;
import org.example.view.OrderOutputView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    private record Context(OrderController controller, InMemoryOrderRepository orderRepo, ByteArrayOutputStream output) {}

    private Context buildContext(String input, InMemorySampleRepository sampleRepo, InMemoryOrderRepository orderRepo) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OrderInputView inputView = new OrderInputView(scanner);
        OrderOutputView outputView = new OrderOutputView(new PrintStream(baos, true, StandardCharsets.UTF_8));
        OrderIdGenerator idGenerator = new OrderIdGenerator(LocalDate.of(2026, 6, 12));
        return new Context(new OrderController(sampleRepo, orderRepo, idGenerator, inputView, outputView), orderRepo, baos);
    }

    @Test
    @DisplayName("N 확인 입력 시 주문이 저장되지 않고 취소 메시지가 출력된다")
    void placeOrder_withCancelConfirm_doesNotSave() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 100));
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        String input = "S-001\n한국반도체연구소\n200\nN\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo);

        ctx.controller().run();

        assertTrue(orderRepo.findAll().isEmpty());
        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("취소"));
    }

    @Test
    @DisplayName("등록되지 않은 시료 ID 입력 시 오류 메시지를 출력하고 저장하지 않는다")
    void placeOrder_withUnknownSampleId_showsError() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        String input = "S-999\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo);

        ctx.controller().run();

        assertTrue(orderRepo.findAll().isEmpty());
        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("S-999"));
    }

    @Test
    @DisplayName("유효한 입력 후 Y 확인 시 RESERVED 주문이 저장된다")
    void placeOrder_withValidInput_savesReservedOrder() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 100));
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();

        String input = "S-001\n한국반도체연구소\n200\nY\n";
        Context ctx = buildContext(input, sampleRepo, orderRepo);

        ctx.controller().run();

        List<Order> orders = orderRepo.findAll();
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.RESERVED, orders.get(0).getStatus());
    }
}
