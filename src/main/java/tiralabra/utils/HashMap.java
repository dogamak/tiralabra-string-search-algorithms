/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.utils;

/**
 * Simple HashMap implementation.
 *
 * Each occupied entry in the internal table stores a key and a value.
 * When looking for the entry associated with a key, the key is first
 * hashed after which the hash is divided by the table's size. The
 * remainder serves as the starting index of the search. 
 *
 * The search checks every entry from the initial index onwards and
 * searches for the first entry which is not occupied or whose key matches
 * the hashed key.
 */
public class HashMap<K, V> {
  /**
   * A single entry in the HashMap's internal table.
   */
  public class Entry {
    /** Key of the value stored in this entry. */
    K key;

    /** The value stored in this entry. */
    V value;

    /**
     * Create an entry from a key and the associated value.
     */
    Entry(K k, V v) {
      key = k;
      value = v;
    }
  }

  /**
   * The internal table which contains key-value pairs in indexes
   * determined by the key's hashes.
   */
  private Object[] table;

  /**
   * Number of occupied entries in {@link #table}.
   *
   * Used to determine when the internal table should be grown.
   * If the number of occipied entries is high relative to the table's overall size,
   * there will be more collisions and look-ups will be slower.
   */
  private int occupied = 0;

  /**
   * Creates a new HashMap.
   *
   * The default initial size for the internal table is 128.
   */
  public HashMap() {
    table = new Object[128];

    for (int i = 0; i < 128; i++)
      table[i] = null;
  }

  private int hash(Object key) {
    int code = key.hashCode();

    if (code < 0)
      code = -code;

    return code;
  }

  /**
   * Inserts or replaces the value associated with the provided key.
   */
  public void insert(K key, V value) {
    int i = hash(key) % table.length;

    while (table[i] != null && ((Entry) table[i]).key.equals(key)) i++;

    table[i] = new Entry(key, value);
    occupied += 1;

    if (occupied > table.length * 0.75) {
      grow();
    }
  }

  /**
   * Doubles the size of the internal table and migrates
   * entries from the old table to the new table.
   */
  private void grow() {
    Object[] oldTable = table;
    table = new Object[oldTable.length * 2];
    occupied = 0;

    for (int i = 0; i < oldTable.length; i++) {
      Entry entry = (Entry) oldTable[i];

      if (entry != null) {
        insert(entry.key, entry.value);
      }
    }
  }

  /**
   * Gets the value associated with a key.
   *
   * If no value is associated with the key, a null is returned.
   */
  public V get(K key) {
    int i = hash(key) % table.length;

    Entry entry;

    do {
      entry = (Entry) table[i];
      i++;
    } while (entry != null && !entry.key.equals(key));

    if (entry == null) {
      return null;
    }

    return entry.value;
  }
}
