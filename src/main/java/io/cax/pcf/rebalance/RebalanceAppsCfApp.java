package io.cax.pcf.rebalance;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.RestartApplicationInstanceRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
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

    @Bean
    CloudFoundryClient cloudFoundryClient(@Value("${cf.host}") String host,
                                          @Value("${cf.username}") String username,
                                          @Value("${cf.password}") String password) {
        return SpringCloudFoundryClient.builder()
                .host(host)
                .skipSslValidation(true)
                .username(username)
                .password(password)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(RebalanceAppsCfApp.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {


        CloudFoundryOperations cloudFoundryOperations = new CloudFoundryOperationsBuilder()
                .cloudFoundryClient(cloudFoundryClient)
                .target("pcfdev-org", "pcfdev-space")
                .build();


        cloudFoundryOperations
                .applications()
                .list()
                .stream()
                .forEach(e -> IntStream.range(0, e.getInstances()).forEach(i -> cloudFoundryOperations
                        .applications()
                        .restartInstance(RestartApplicationInstanceRequest
                                .builder()
                                .name(e.getName())
                                .instanceIndex(i)
                                .build()).get()));


    }
}