package com.example.fbs.orderprocessing.integration;

import com.example.fbs.orderprocessing.model.AssemblyTask;

/** Передаёт задание на сборку в блок A4. В рамках A3 — заглушка. */
public interface AssemblyTaskPublisher {

    void publish(AssemblyTask task);
}
