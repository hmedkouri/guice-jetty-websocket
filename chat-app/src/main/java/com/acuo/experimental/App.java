package com.acuo.experimental;

import com.acuo.common.app.jetty.JettyResourceHandlerConfig;
import com.acuo.common.app.jetty.JettyServerConnectorConfig;
import com.acuo.common.app.jetty.JettyServerWrapperConfig;
import com.acuo.common.app.main.Main;
import com.acuo.experimental.tyrus.TyrusModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.eclipse.jetty.util.resource.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Collection;

public class App extends Main {

    static class JettyServerWrapperConfigProvider implements Provider<JettyServerWrapperConfig> {

        private final String resourceDir;

        @Inject
        JettyServerWrapperConfigProvider(@Named("dir") String resourceDir) {
            this.resourceDir = resourceDir;
        }

        public JettyServerWrapperConfig get() {

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
    }


    @Override
    public Class<? extends Provider<JettyServerWrapperConfig>> config() {
        return JettyServerWrapperConfigProvider.class;
    }

    @Override
    public Collection<Module> modules() {
        return ImmutableList.of(new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named("dir")).to("/tyrus");
            }
        }, new TyrusModule());
    }

    public static void main(String[] args) {
        App app = new App();
        app.startAsync();
    }

}