package rs.ac.bg.etf.aor2.replacementpolicy;

import rs.ac.bg.etf.aor2.memory.MemoryOperation;
import rs.ac.bg.etf.aor2.memory.cache.ICacheMemory;
import rs.ac.bg.etf.aor2.memory.cache.Tag;

import java.util.List;

public class LRUKReplacementPolicy implements IReplacementPolicy {
    // NAPOMENA: Ovo nije bas kao LRU-K sa prezentacija, vec vise nalici LRU
    // aproksimaciji sa bitima referenciranja kakva se radi na OS2 (jer je jednostavnije za implementaciju, ali
    // dosta slicno)
    // ISTO BITNO: pomeranje bi trebalo da se radi PERIODICNO, ne pri svakom pristupu, ovaj algoritam skoro sigurno
    // nece dolaziti na labu, ali ostavljam za posmatranje

    @Override
    public String getName() {
        return "LRU-K";
    }

    @Override
    public void init(ICacheMemory cacheMemory) {
        this.cacheMemory = cacheMemory;

        setAsoc = (int) cacheMemory.getSetAsociativity();

        counters = new int[(int) cacheMemory.getBlockNum()];
    }

    @Override
    public int getBlockIndexToReplace(long adr) {
        // vrati registar koji ima minimalnu vrednost u setu

        int set = (int) cacheMemory.extractSet(adr);

        int minBlock = -1;
        int minVal = Integer.MAX_VALUE;

        for (int i = set * setAsoc; i < (set + 1) * setAsoc; i++) {
            if (counters[i] < minVal) {
                minVal = counters[i];
                minBlock = i;
            }
        }

        return minBlock;
    }

    @Override
    public void doOperation(MemoryOperation operation) {
        int adr = (int) operation.getAddress();
        int set = (int) cacheMemory.extractSet(adr);
        int tagVal = (int) cacheMemory.extractTag(adr);

        List<Tag> tags = cacheMemory.getTags();

        for (int i = set * setAsoc; i < (set + 1) * setAsoc; i++) {
            // shift all entries by one
            counters[i] >>= 1;
            Tag tag = tags.get(i);

            // and if accessed add accessed bit
            if (tag.V && tag.tag == tagVal) {
                counters[i] |= (1 << 31);
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

    private int[] counters; // K == 32

    private int setAsoc;

    private ICacheMemory cacheMemory;
}
