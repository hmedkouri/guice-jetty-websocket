package com.acuo.experimental.tyrus;

import com.google.inject.AbstractModule;

public class TyrusModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ChatEndpoint.class).asEagerSingleton();
    }
}