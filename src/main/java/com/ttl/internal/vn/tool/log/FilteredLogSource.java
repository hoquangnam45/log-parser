package com.ttl.internal.vn.tool.log;

import java.util.Iterator;
import java.util.stream.Stream;

public class FilteredLogSource implements ILogSource {
    public FilteredLogSource(Stream<ILogEntry> filteredStream) {

    }

    @Override
    public String getID()
    {
        return null;
    }

    @Override
    public void refresh()
    {

    }

    @Override
    public Iterator<ILogEntry> iterator()
    {
        return null;
    }
}
