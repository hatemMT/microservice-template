package com.sumerge.service.registery;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.ImmutableRegCheck;
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
    private String contextPath;

    AgentClient agentClient;


    @PostConstruct
    public void register() {
        logger.info("Consul register url : {}", registryUrl);

        Consul consul = Consul.builder().withUrl(registryUrl).build();
        agentClient = consul.agentClient();
        logger.info("check health url :::::::::::::::: {}:{}/{}/resources/health", webHost, webPort, contextPath);
        ImmutableRegCheck check = ImmutableRegCheck
                .builder()
                .http(webHost + ":" + webPort + "/" + contextPath + "/resources/health")
                .interval("5s")
                .build();

        final ImmutableRegistration registration = ImmutableRegistration.builder()
                .id(contextPath)
                .name(contextPath)
                .address(webHost)
                .port(Integer.parseInt(webPort))
                .addChecks(check)
                .build();
        agentClient.register(registration);
//        try {
//            agentClient.pass(contextPath);
//        } catch (NotRegisteredException e) {
//            logger.error(e.getMessage(), e);
//        }

        logger.info("{} is registered in consul on {} : {}", contextPath, webHost, webPort);
    }

    @PreDestroy
    public void unRegister() {
        agentClient.deregister(contextPath);
        logger.info("{} is un-registered from consul", contextPath);
    }
}
