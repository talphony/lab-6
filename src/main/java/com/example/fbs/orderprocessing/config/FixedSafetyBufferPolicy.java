package com.example.fbs.orderprocessing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Заглушка страхового буфера: одно число для всех артикулов из настройки wms.safety-buffer. */
@Component
public class FixedSafetyBufferPolicy implements SafetyBufferPolicy {

    private final int buffer;

    public FixedSafetyBufferPolicy(@Value("${wms.safety-buffer:0}") int buffer) {
        if (buffer < 0) {
            throw new IllegalArgumentException("Страховой буфер не может быть отрицательным");
        }
        this.buffer = buffer;
    }

    @Override
    public int bufferFor(String sku) {
        return buffer;
    }
}
