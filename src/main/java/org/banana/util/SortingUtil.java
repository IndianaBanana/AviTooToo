package org.banana.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

@Slf4j
public abstract class SortingUtil {

    public static <T> List<T> sortBy(List<T> items, List<Comparator<T>> comparators) {
        log.debug("entering sortBy method in SortingUtil");
        if (comparators == null || comparators.isEmpty()) {
            return items;
        }

        Comparator<T> combinedComparator = comparators.get(0);
        for (int i = 1; i < comparators.size(); i++) {
            combinedComparator = combinedComparator.thenComparing(comparators.get(i));
        }

        return items.stream()
                .sorted(combinedComparator)
                .toList();
    }
}
