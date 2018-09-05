package com.graphdb.index;

import com.graphdb.utils.EventType;
import com.lmax.disruptor.EventHandler;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by mishkat on 8/21/17.
 *
 * @author mishkat, Ashraful Islam
 */
public class IndexEventHandler implements EventHandler<IndexEvent>, Closeable {

    private final GraphDAO graphDAO;

    public IndexEventHandler() {
        graphDAO = new GraphDAO();
    }

    public void onEvent(IndexEvent indexEvent, long sequence, boolean endOfBatch) throws Exception {
        if (indexEvent.getEventType() == EventType.DATA) {
            if (!graphDAO.crud(indexEvent.getId(), indexEvent.getType(), indexEvent.getKeyValues())) {
                graphDAO.errorLog(indexEvent);
            }
        }
    }

    @Override
    public void close() throws IOException {
        graphDAO.close();
    }
}