package com.acuo.experimental;

import com.acuo.common.app.jetty.JettyResourceHandlerConfig;
import com.acuo.common.app.jetty.JettyServerConnectorConfig;
import com.acuo.common.app.jetty.JettyServerWrapperConfig;
import com.acuo.common.app.main.Main;
import com.acuo.experimental.tyrus.TyrusModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import org.eclipse.jetty.util.resource.Resource;

import java.util.Collection;

public class App extends Main {

    private JettyServerWrapperConfig config(String resourceDir) {

        JettyResourceHandlerConfig chat = new JettyResourceHandlerConfig()
                .withBaseResource(Resource.newClassPathResource(resourceDir))
                .withWelcomeFiles(Lists.newArrayList("index.html"))
                .withEtags(true)
                .withContextPath("/");

        return new JettyServerWrapperConfig()
                .withContextPath("/")
                .withApiPath("/api")
                .withWebSocketSupport()
                .withResourceHandlerConfig(chat)
                .withHttpServerConnectorConfig(JettyServerConnectorConfig.forHttp("localhost", 8080));
    }


    @Override
    public JettyServerWrapperConfig config() {
        return config("/tyrus");
    }

    @Override
    public Collection<Module> modules() {
        return ImmutableList.of(new TyrusModule());
    }

    public static void main(String[] args) {
        App app = new App();
        app.startAsync();
    }

}