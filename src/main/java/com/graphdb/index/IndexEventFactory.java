package com.graphdb.index;

import com.lmax.disruptor.EventFactory;

/**
 * Created by mishkat on 8/21/17.
 *
 * @author mishkat, Ashraful Islam
 */
public class IndexEventFactory implements EventFactory<IndexEvent> {

    private IndexEventFactory() {
    }

    public static IndexEventFactory create() {
        return new IndexEventFactory();
    }

    public IndexEvent newInstance() {
        return new IndexEvent();
    }
}
