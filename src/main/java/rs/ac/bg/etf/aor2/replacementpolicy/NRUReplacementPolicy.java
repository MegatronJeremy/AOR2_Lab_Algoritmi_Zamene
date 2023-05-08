package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;
import java.util.List;

public class NRUReplacementPolicy implements IReplacementPolicy {
    @Override
    public String getName() {
        return "NRU";
    }

    @Override
    public void init(ICacheMemory cacheMemory) {
        setAsoc = cacheMemory.getSetAsociativity();
        iCacheMemory = cacheMemory;

        for (long i = 0; i < cacheMemory.getBlockNum(); i++) {
            entryAccesed.add(false);
        }
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        // return least significant zero entry

        long set = iCacheMemory.extractSet(adr);

        for (long i = 0; i < setAsoc; i++) {
            long block = set * setAsoc + i;

            if (!entryAccesed.get((int) block)) {
                return (int) block;
            }
        }

        return 0;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        long adr = operation.getAddress();
        long set = iCacheMemory.extractSet(adr);
        long tagVal = iCacheMemory.extractTag(adr);

        List<Tag> tags = iCacheMemory.getTags();

        // set the entry to accessed
        long entry = 0;
        for (long i = 0; i < setAsoc; i++) {
            long block = i + set * setAsoc;

            Tag tag = tags.get((int) block);

            if (tag.V && tag.tag == tagVal) {
                // found entry

                entryAccesed.set((int) block, true);
                entry = i;
                break;
            }
        }

        // check if all entries are accessed
        for (long i = 0; i < setAsoc; i++) {
            long block = i + set * setAsoc;

            if (!entryAccesed.get((int) block)) {
                return;
            }
        }

        // if all accessed reset set entries
        for (long i = 0; i < setAsoc; i++) {
            if (i == entry)
                continue; // except the last one accessed

            long block = i + set * setAsoc;

            entryAccesed.set((int) block, false);
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

    private final List<Boolean> entryAccesed = new ArrayList<>();
    private long setAsoc;
    private ICacheMemory iCacheMemory;
}
