package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.List;

public class LFUReplacementPolicy implements IReplacementPolicy {
    @Override
    public String getName() {
        return "LFU";
    }

    @Override
    public void init(ICacheMemory cacheMemory) {
        iCacheMemory = cacheMemory;
        int blockNum = (int) cacheMemory.getBlockNum();
        setAsoc = (int) cacheMemory.getSetAsociativity();

        counters = new int[blockNum];
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        int set = (int) iCacheMemory.extractSet(adr);

        // find least frequently used block
        int minCounter = Integer.MAX_VALUE;
        int minBlock = -1;
        for (int i = 0; i < setAsoc; i++) {
            int block = i + set * setAsoc;

            if (counters[block] < minCounter) {
                minCounter = counters[block];
                minBlock = block;
            }
        }

        counters[minBlock] = 0;

        return minBlock;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        int adr = (int) operation.getAddress();
        int set = (int) iCacheMemory.extractSet(adr);
        int tagVal = (int) iCacheMemory.extractTag(adr);


        // find entry by tag and increase it by one
        List<Tag> tags = iCacheMemory.getTags();

        for (int i = set * setAsoc; i < (set + 1) * setAsoc; i++) {
            Tag tag = tags.get(i);

            if (tag.V && tag.tag == tagVal) {
                counters[i]++;
                break;
            }
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

    private int setAsoc;

    private int[] counters;
}
