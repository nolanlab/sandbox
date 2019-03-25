/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Map.Entry;

/**
 *
 * @author Nikolay
 */
public class DefaultEntry<K, V> implements Entry<K, V> {

    private K key;
    private V value;

    public DefaultEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public K setKey(K newKey) {
        K oldKey = this.key;
        this.key = newKey;
        return oldKey;
    }
}
