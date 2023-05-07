package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OneBitReplacementPolicy implements IReplacementPolicy {
    @Override
    public String getName() {
        return "One Bit";
    }

    @Override
    public void init(ICacheMemory cacheMemory) {
        iCacheMemory = cacheMemory;

        setAsoc = (int) iCacheMemory.getSetAsociativity();
        int setNum = (int) iCacheMemory.getSetNum();

        for (int i = 0; i < setNum; i++) {
            lruBits.add(0);
        }
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        int set = (int) iCacheMemory.extractSet(adr); // koji set posmatram

        ArrayList<Tag> tags = iCacheMemory.getTags();

        int result = -1;
        for (int i = 0; i < setAsoc; i++) {
            if (!tags.get(set * setAsoc + i).V) { // tagovi za sve blokove u setu
                result = i; // set * setAsoc -> pocetak blokova ovog seta
                break;
            }
        }
        if (result == -1) {
            // biraj blok za zamenu
            result = (int) ((setAsoc / 2) * lruBits.get(set) + setAsoc / 2 * Math.random());
        }

        return set * setAsoc + result;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        long adr = operation.getAddress();

        int set = (int) iCacheMemory.extractSet(adr); // koji set posmatram

        // dalje moram naci kojoj grupi pripada
        // i postavljam bit - 1 donja grupa, 0 gornja grupa

        int tagData = (int) iCacheMemory.extractTag(adr);

        List<Tag> tagList = iCacheMemory.getTags();

        int entry = 0;
        for (int i = 0; i < setAsoc; i++) {
            int block = set * setAsoc + i;

            Tag tag = tagList.get(block);

            if (tag.V && tag.tag == tagData) {
                // found entry
                entry = i;
                break;
            }
        }

        if (entry < setAsoc / 2) {
            lruBits.set(set, 1); // choose upper half next
        } else {
            lruBits.set(set, 0); // choose lower half next
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
    private final List<Integer> lruBits = new ArrayList<>();

    private int setAsoc;

}
