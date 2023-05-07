package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;
import java.util.List;

public class SIDEReplacementPolicy implements IReplacementPolicy {
    @Override
    public String getName() {
        return "SIDE";
    }

    @Override
    public void init(ICacheMemory cacheMemory) {
        cm = cacheMemory;
        setAsoc = (int) cacheMemory.getSetAsociativity();
        int setNum = (int) cacheMemory.getSetNum();

        for (int i = 0; i < setNum; i++) {
            counters.add(0);
        }
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        int set = (int) cm.extractSet(adr);

        // block for eviction is the current counter value
        int entry = counters.get(set);

        return entry + set * setAsoc;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        int adr = (int) operation.getAddress();
        int set = (int) cm.extractSet(adr);
        int tagVal = (int) cm.extractTag(adr);

        // find entry this belongs to

        int entry = 0;

        List<Tag> tags = cm.getTags();

        for (int i = 0; i < setAsoc; i++) {
            int block = i + set * setAsoc;

            Tag t = tags.get(block);

            if (t.V && t.tag == tagVal) {
                entry = i;
                break;
            }
        }

        if (entry >= counters.get(set)) {
            counters.set(set, (counters.get(set) + 1) % setAsoc);
        }
    }

    @Override
    public String printValid() {
        return null;
    }

    @Override
    public String printAll() {
        return null;
    }

    @Override
    public void reset() {

    }

    private final List<Integer> counters = new ArrayList<>();

    private int setAsoc;

    private ICacheMemory cm;
}
