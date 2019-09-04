package de.microbob.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Servers implements Iterable<Server> {
    private List<Server> serverList = new ArrayList<>();

    public boolean add(Server server) {
        return serverList.add(server);
    }

    public boolean remove(Server server) {
        return serverList.remove(server);
    }

    public List<Server> getServerList() {
        return serverList;
    }

    @Override
    public Iterator<Server> iterator() {
        return serverList.iterator();
    }
}
