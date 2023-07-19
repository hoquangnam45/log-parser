package com.ttl.internal.vn.tool.cli;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.Flow;

public class LogFile implements ILogSource {
    private String hash;
    private String path;

    public LogFile(File file) {
        this.path = path;
    }

    @Override
    public byte[] read() {
        return new byte[0];
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void subscribe(Flow.Subscriber<byte[]> subscriber) {

    }

    @Override
    public void join(ILogSource anotherSource) {

    }

    @Override
    public void reset() {

    }
}