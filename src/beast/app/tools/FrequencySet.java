package beast.app.tools;

import java.util.*;

/**
 *
 */

public class FrequencySet<T> {

    public FrequencySet() {}

    /** get number of objects */
    public int size()
    {
        return size;
    }

    /** get object in frequency order */
    public T get(int i) {
        if (!sorted) {
            sortByFrequency();
        }

        return list.get(i).object;
    }


    protected Integer getFrequency(Object b) {
        Bin bin = hash.get(b.toString());
        if( bin == null ) {
            return null;
        }
        return bin.frequency;
    }

    /** get frequency of ith object */
    public int getFrequency(int i)
    {
        if (!sorted) {
            sortByFrequency();
        }

        return list.get(i).frequency;
    }

    /** get sum of all frequencies */
    public int getSumFrequency() {
        int sum = 0;
        for (int i = 0, n = size(); i < n; i++) {
            sum += getFrequency(i);
        }

        return sum;
    }

    public void add(T object) {
        add(object, 1);
    }

    public void add(T object, int frequency) {

        Bin bin = hash.get(object.toString());
        if (bin != null) {
            bin.frequency += 1;
        } else {
            bin = new Bin(object, frequency);
            hash.put(object.toString(), bin);
            size += 1;
            sorted = false;
        }
    }

    private void sortByFrequency() {

        list.clear();
        for (Bin bin : hash.values()) {
            list.add(bin);
        }

        Collections.sort(list, frequencyComparator);
        sorted = true;
    }

    private List<Bin> list = new ArrayList<Bin>();
    private Hashtable<String, Bin> hash = new Hashtable<String, Bin>();
    private HashSet set = new HashSet();
    private boolean sorted = false;
    private int size = 0;

    private class Bin {
        T object;
        int frequency;

        public Bin(T object, int frequency) {
            this.object = object;
            this.frequency = frequency;
        }

    }

    private Comparator<Bin> frequencyComparator = new Comparator<Bin>() {
        public int compare(Bin bin1, Bin bin2) {
            return bin2.frequency - bin1.frequency;
        }
    };
}
