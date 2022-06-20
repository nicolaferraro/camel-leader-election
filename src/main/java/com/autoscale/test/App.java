package com.autoscale.test;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cluster.CamelClusterEventListener;
import org.apache.camel.cluster.CamelClusterService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {
        public void configure() throws Exception {

            from("master:lock1:timer:clock")
              .log("Hello World!");

        }
    }


    @Configuration
    static class BeanConfiguration {

        @Bean
        public CustomService customService(CamelClusterService clusterService) throws Exception {
            CustomService service = new CustomService();
            clusterService.getView("lock2").addEventListener((CamelClusterEventListener.Leadership) (view, leader) -> {
                boolean weAreLeaders = leader.isPresent() && leader.get().isLocal();
                if (weAreLeaders && !service.isStarted()) {
                    service.start();
                } else if (!weAreLeaders && service.isStarted()) {
                    service.stop();
                }
            });
            return service;
        }

    }


}
