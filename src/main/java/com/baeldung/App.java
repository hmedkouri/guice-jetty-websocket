package com.baeldung;

import com.acuo.common.app.main.Main;
import com.acuo.common.app.main.ResteasyConfig;
import com.acuo.common.http.server.HttpResourceHandlerConfig;
import com.acuo.common.http.server.HttpServerConnectorConfig;
import com.acuo.common.http.server.HttpServerWrapperConfig;
import com.baeldung.websocket.ChatModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import org.eclipse.jetty.util.resource.Resource;

import java.util.Collection;

public class App extends Main {

    static class AppConfig implements ResteasyConfig {

        private final HttpServerWrapperConfig config;

        AppConfig() {

            HttpResourceHandlerConfig chat = new HttpResourceHandlerConfig()
                    .withBaseResource(Resource.newClassPathResource("/chat"))
                    .withWelcomeFiles(Lists.newArrayList("index.html"))
                    .withEtags(true)
                    .withContextPath("/");

            config = new HttpServerWrapperConfig()
                    .withWebSocketSupport()
                    .withResourceHandlerConfig(chat)
                    .withHttpServerConnectorConfig(HttpServerConnectorConfig.forHttp("localhost", 8080));
            config.setContextPath("/");
            config.setApiPath("/api");
        }

        @Override
        public HttpServerWrapperConfig getConfig() {
            return config;
        }
    }

    @Override
    public ResteasyConfig config() {
        return new AppConfig();
    }

    @Override
    public Collection<Module> modules() {
        return ImmutableList.of(new ChatModule());
    }

    public static void main(String[] args) {
        App app = new App();
        app.startAsync();
    }

}