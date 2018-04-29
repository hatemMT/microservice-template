package com.sumerge.service.registery;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import static com.orbitz.consul.model.agent.Registration.RegCheck.http;

@Singleton
@Startup
public class ApplicationRegisterer {

    @Inject
    Logger logger;

    @Inject
    @ConfigProperty(name = "registry.url")
    private String registryUrl;

    @Inject
    @ConfigProperty(name = "web.host")
    private String webHost;

    @Inject
    @ConfigProperty(name = "web.port")
    private String webPort;

    @Inject
    @ConfigProperty(name = "context.path")
    private String registryService;

    AgentClient agentClient;


    @PostConstruct
    public void register() {
        logger.info("Consul register url : {}", registryUrl);

        Consul consul = Consul.builder().withUrl(registryUrl).build();
        agentClient = consul.agentClient();

        final ImmutableRegistration registration = ImmutableRegistration.builder()
                .id(registryService)
                .name(registryService)
                .address(webHost)
                .port(Integer.parseInt(webPort))
                .check(http(webHost + ":" + webPort + "/" + registryService + "/resources/health", 5))
                .build();
        agentClient.register(registration);

        logger.info("{0} is registered in consul on {1} : {2}", registryService, webHost, webPort);
    }

    @PreDestroy
    public void unRegister() {
        agentClient.deregister(registryService);
        logger.info("{} is un-registered from consul", registryService);
    }
}
