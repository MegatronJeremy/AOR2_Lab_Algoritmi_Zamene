package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;
import java.util.List;

public class MPLRUReplacementPolicy implements IReplacementPolicy {
    @Override
    public String getName() {
        return "MPLRU";
    }

    @Override
    public void init(ICacheMemory cacheMemory) {
        // broj bita po setu == setAsocijativnost

        iCacheMemory = cacheMemory;
        setAsoc = (int) cacheMemory.getSetAsociativity();

        for (long i = 0; i < cacheMemory.getSetNum(); i++) {
            List<Boolean> entryBitSet = new ArrayList<>();
            for (long j = 0; j < cacheMemory.getBlockNum(); j++) {
                entryBitSet.add(false);
            }
            entryBits.add(entryBitSet);
        }
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        int set = (int) iCacheMemory.extractSet(adr);

        List<Tag> tags = iCacheMemory.getTags();

        // check if any entries are not valid
        for (int i = 0; i < setAsoc; i++) {
            int block = i + set * setAsoc;

            Tag tag = tags.get(block);

            if (!tag.V) return block;
        }

        List<Boolean> entryBitSet = entryBits.get(set);

        // choose a block for replacement using the PREVIOUS BIT AS THE FIRST ONE
        int bit = 1;
        int entry = 0;
        int inc = setAsoc / 2;
        while (bit < entryBitSet.size()) {
            if (entryBitSet.get(bit)) {
                bit++; // go to left child if true, right was accessed
                entry += inc;
            }
            bit *= 2;
            inc /= 2;
        }

        return entry + set * setAsoc;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        int adr = (int) operation.getAddress();
        int set = (int) iCacheMemory.extractSet(adr);
        int tagVal = (int) iCacheMemory.extractTag(adr);

        List<Tag> tags = iCacheMemory.getTags();

        // find the entry first
        int entry = 0;
        for (; entry < setAsoc; entry++) {
            int block = entry + set * setAsoc;

            Tag tag = tags.get(block);

            if (tag.V && tag.tag == tagVal) {
                break;
            }
        }

        List<Boolean> entryBitSet = entryBits.get(set);

        if (entryBitSet.size() > 1) {
            // copy current bit to the previous bit
            entryBitSet.set(1, entryBitSet.get(0));
        }
        int bit = 0;
        int inc = setAsoc / 4;
        int searchVal = setAsoc / 2;
        while (bit < entryBitSet.size()) {
            int add = 0;
            if (entry >= searchVal) {
                // go right
                entryBitSet.set(bit, true);
                searchVal += inc;
                add += 1;
            } else {
                searchVal -= inc;
                entryBitSet.set(bit, false);
            }
            if (bit == 0) {
                bit += 1;
            }
            bit = bit * 2 + add;
            inc /= 2;
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

    private ICacheMemory iCacheMemory;
    private final List<List<Boolean>> entryBits = new ArrayList<>();

    private int setAsoc;

}
