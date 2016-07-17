package io.cax.pcf.rebalance;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.Collectors;


@SpringBootApplication
public class RebalanceAppsCfApp implements CommandLineRunner {

    static final Logger LOG = LoggerFactory.getLogger(RebalanceAppsCfApp.class);


    @Autowired
    CloudFoundryClient cloudFoundryClient;

    @Autowired
    CloudFoundryOperations cloudFoundryOperations;

    @Bean
    DefaultConnectionContext connectionContext(@Value("${cf.api}") String apiHost) {
        return DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .skipSslValidation(true)
                .build();
    }

    @Bean
    PasswordGrantTokenProvider tokenProvider(@Value("${cf.username}") String username,
                                             @Value("${cf.password}") String password) {
        return PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build();
    }

    @Bean
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }

    @Bean
    CloudFoundryOperations cloudFoundryOperations(@Value("${cf.org}") String organisation,
                                                  @Value("${cf.space}") String space) {

        return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .organization(organisation)
                .space(space)
                .build();
    }


    public static void main(String[] args) {
        SpringApplication.run(RebalanceAppsCfApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {


        cloudFoundryOperations
                .applications()
                .list()
                .toStream().collect(Collectors.toList())
                .forEach(applicationSummary -> {
                    cloudFoundryOperations.applications().restart(RestartApplicationRequest
                            .builder()
                            .name(applicationSummary.getName())
                            .build()).subscribe(new MySubscriber(applicationSummary));
                });


    }

    private static class MySubscriber implements Subscriber<Void> {
        private final ApplicationSummary applicationSummary;

        public MySubscriber(ApplicationSummary applicationSummary) {
            this.applicationSummary = applicationSummary;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
        }

        @Override
        public void onNext(Void aVoid) {
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("App restart error", throwable);
            LOG.error("app: " + applicationSummary.getName() + " error!!");
        }

        @Override
        public void onComplete() {
            LOG.info("app: " + applicationSummary.getName() + " started!!");
        }
    }
}