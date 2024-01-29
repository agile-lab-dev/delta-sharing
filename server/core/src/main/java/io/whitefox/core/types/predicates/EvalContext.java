package io.whitefox.core.types.predicates;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class EvalContext {

    public EvalContext(
            Map<String, String> partitionValues, Map<String, Pair<String, String>> statsValues) {
        this.partitionValues = partitionValues;
        this.statsValues = statsValues;
    }

    final Map<String, String> partitionValues;
    final Map<String, Pair<String, String>> statsValues;

    public Map<String, String> getPartitionValues() {
        return partitionValues;
    }

    public Map<String, Pair<String, String>> getStatsValues() {
        return statsValues;
    }
}