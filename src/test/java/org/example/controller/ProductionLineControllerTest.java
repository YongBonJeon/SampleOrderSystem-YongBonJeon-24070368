package org.example.controller;

import org.example.model.Order;
import org.example.model.OrderStatus;
import org.example.model.ProductionJob;
import org.example.model.Sample;
import org.example.queue.ProductionQueue;
import org.example.repository.impl.InMemoryOrderRepository;
import org.example.repository.impl.InMemorySampleRepository;
import org.example.view.ProductionLineOutputView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ProductionLineControllerTest {

    private record Context(ProductionLineController controller,
                           InMemoryOrderRepository orderRepo,
                           InMemorySampleRepository sampleRepo,
                           ByteArrayOutputStream output) {}

    private Context buildContext(InMemorySampleRepository sampleRepo,
                                 InMemoryOrderRepository orderRepo,
                                 ProductionQueue queue,
                                 Supplier<LocalDateTime> clock) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ProductionLineOutputView outputView = new ProductionLineOutputView(
                new PrintStream(baos, true, StandardCharsets.UTF_8));
        return new Context(
                new ProductionLineController(sampleRepo, orderRepo, queue, outputView, clock),
                orderRepo, sampleRepo, baos);
    }

    @Test
    @DisplayName("큐가 비어있을 때 안내 메시지를 출력하고 복귀한다")
    void run_withEmptyQueue_showsNoJobMessage() {
        Context ctx = buildContext(new InMemorySampleRepository(),
                new InMemoryOrderRepository(), new ProductionQueue(), LocalDateTime::now);

        ctx.controller().run();

        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("없"));
    }

    @Test
    @DisplayName("경과 시간이 총 생산 시간을 넘으면 자동으로 재고가 증가하고 주문이 CONFIRMED로 전환된다")
    void run_whenTimeElapsed_autoCompletes() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 30));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.PRODUCING));

        // shortage=170, yieldRate=0.9, avgTime=0.5 → actualQty=210, totalTime=105.0 min
        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);

        LocalDateTime enqueueTime = LocalDateTime.of(2026, 6, 12, 9, 0);
        AtomicReference<LocalDateTime> timeRef = new AtomicReference<>(enqueueTime);
        Supplier<LocalDateTime> clock = timeRef::get;

        ProductionQueue queue = new ProductionQueue(clock);
        queue.enqueue(job); // startedAt = 09:00

        // 106분 경과 (totalTime = 105 min) → 완료 조건 충족
        timeRef.set(enqueueTime.plusMinutes(106));

        Context ctx = buildContext(sampleRepo, orderRepo, queue, clock);
        ctx.controller().run();

        assertEquals(OrderStatus.CONFIRMED, orderRepo.findById("ORD-20260612-0001").get().getStatus());
        assertEquals(30 + 210, sampleRepo.findById("S-001").get().getStock());
    }

    @Test
    @DisplayName("경과 시간이 총 생산 시간 미만이면 상태 변경 없이 진행 현황을 출력한다")
    void run_whenJobInProgress_doesNotComplete() {
        InMemorySampleRepository sampleRepo = new InMemorySampleRepository();
        sampleRepo.save(new Sample("S-001", "Silicon-Wafer", 0.5, 0.9, 30));

        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        orderRepo.save(new Order("ORD-20260612-0001", "S-001", "한국반도체연구소", 200, OrderStatus.PRODUCING));

        ProductionJob job = new ProductionJob("ORD-20260612-0001", "S-001", 170, 0.9, 0.5);

        LocalDateTime enqueueTime = LocalDateTime.of(2026, 6, 12, 9, 0);
        AtomicReference<LocalDateTime> timeRef = new AtomicReference<>(enqueueTime);
        Supplier<LocalDateTime> clock = timeRef::get;

        ProductionQueue queue = new ProductionQueue(clock);
        queue.enqueue(job); // startedAt = 09:00

        // 50분 경과 (totalTime = 105 min) → 아직 생산 중
        timeRef.set(enqueueTime.plusMinutes(50));

        Context ctx = buildContext(sampleRepo, orderRepo, queue, clock);
        ctx.controller().run();

        assertEquals(OrderStatus.PRODUCING, orderRepo.findById("ORD-20260612-0001").get().getStatus());
        assertEquals(30, sampleRepo.findById("S-001").get().getStock());
        assertTrue(ctx.output().toString(StandardCharsets.UTF_8).contains("%"));
    }
}
