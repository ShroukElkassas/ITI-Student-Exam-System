package iti.exam.desktop.db;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class StoredProcResult {
    private final int updateCount;
    private final List<List<Map<String, Object>>> resultSets;
    private final List<Object> outValues;

    public StoredProcResult(int updateCount, List<List<Map<String, Object>>> resultSets, List<Object> outValues) {
        this.updateCount = updateCount;
        this.resultSets = resultSets == null ? Collections.<List<Map<String, Object>>>emptyList() : resultSets;
        this.outValues = outValues == null ? Collections.<Object>emptyList() : outValues;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public List<List<Map<String, Object>>> getResultSets() {
        return resultSets;
    }

    public List<Object> getOutValues() {
        return outValues;
    }

    public List<Map<String, Object>> firstResultSetOrEmpty() {
        if (resultSets.isEmpty()) {
            return Collections.emptyList();
        }
        return resultSets.get(0);
    }
}

