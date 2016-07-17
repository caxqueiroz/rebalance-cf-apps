package io.cax.pcf.rebalance;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.RestartApplicationInstanceRequest;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.IntStream;


@SpringBootApplication
public class RebalanceAppsCfApp implements CommandLineRunner {

    static final Logger LOG = LoggerFactory.getLogger(RebalanceAppsCfApp.class);


    @Autowired
    CloudFoundryClient cloudFoundryClient;

    @Autowired
    CloudFoundryOperations cloudFoundryOperations;

    @Bean
    DefaultConnectionContext connectionContext(@Value("${cf.host}") String apiHost) {
        return DefaultConnectionContext.builder()
                .apiHost(apiHost)
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
                .list().toStream()
                .forEach(e -> IntStream.range(0, e.getInstances()).forEach(i -> cloudFoundryOperations
                        .applications()
                        .restartInstance(RestartApplicationInstanceRequest
                                .builder()
                                .name(e.getName())
                                .instanceIndex(i)
                                .build())));


    }
}