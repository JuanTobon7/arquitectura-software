package com.pipefilters.persistence;

public final class Factory {
    public static Repository create() {
        return new H2RepositoryImpl(new DatabaseAccess());
    }
}
