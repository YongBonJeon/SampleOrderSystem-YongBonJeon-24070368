package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.repository.impl.InMemoryOrderRepository;
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
                           ByteArrayOutputStream output) {}

    private Context buildContext(String input, InMemoryOrderRepository orderRepo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ReleaseInputView inputView = new ReleaseInputView(scanner);
        ReleaseOutputView outputView = new ReleaseOutputView(
                new PrintStream(baos, true, StandardCharsets.UTF_8));
        return new Context(new ReleaseController(orderRepo, inputView, outputView), orderRepo, baos);
    }

    @Test
    @DisplayName("CONFIRMED 주문이 없을 때 안내 메시지를 출력하고 복귀한다")
    void run_withNoConfirmedOrders_showsEmptyMessage() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.RESERVED));

        Context ctx = buildContext("", orderRepo);
        ctx.controller().run();

        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("없"));
    }

    @Test
    @DisplayName("유효한 번호 입력 시 주문 상태가 RELEASE로 전환된다")
    void run_withValidNumber_releasesOrder() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.CONFIRMED));

        Context ctx = buildContext("1\n", orderRepo);
        ctx.controller().run();

        assertEquals(OrderStatus.RELEASE, orderRepo.findById("ORD-001").get().getStatus());
    }

    @Test
    @DisplayName("0 입력 시 주문 상태가 변경되지 않는다")
    void run_withCancelInput_doesNotChangeStatus() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-001", "S-001", "고객A", 100, OrderStatus.CONFIRMED));

        Context ctx = buildContext("0\n", orderRepo);
        ctx.controller().run();

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("ORD-001").get().getStatus());
    }
}
