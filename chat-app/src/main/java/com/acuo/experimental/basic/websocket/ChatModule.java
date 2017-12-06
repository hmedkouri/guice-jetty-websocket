package com.acuo.experimental.basic.websocket;

import com.google.inject.AbstractModule;

public class ChatModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ChatEndpoint.class).asEagerSingleton();
    }
}
