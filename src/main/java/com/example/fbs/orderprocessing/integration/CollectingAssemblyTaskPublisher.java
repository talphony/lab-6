package com.example.fbs.orderprocessing.integration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.example.fbs.orderprocessing.model.AssemblyTask;

/** Заглушка передачи в A4: складывает задания в список. */
@Component
public class CollectingAssemblyTaskPublisher implements AssemblyTaskPublisher {

    private final List<AssemblyTask> published = new ArrayList<>();

    @Override
    public synchronized void publish(AssemblyTask task) {
        published.add(task);
    }

    public synchronized void clear() {
        published.clear();
    }

    public synchronized List<AssemblyTask> published() {
        return List.copyOf(published);
    }
}
