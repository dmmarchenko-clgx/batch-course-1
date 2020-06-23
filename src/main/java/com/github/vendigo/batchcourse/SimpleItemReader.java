package com.github.vendigo.batchcourse;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
public class SimpleItemReader implements ItemReader<String> {

    private final List<String> dataSet = Arrays.asList(
        "Kyiv", "Nizhyn", "Odesa", "Lviv", "Chernigiv", "Kovel", "Rivne"
    );
    private final Iterator<String> iterator = dataSet.iterator();

    @Override
    public String read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
