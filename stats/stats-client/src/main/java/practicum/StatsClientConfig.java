package practicum;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = StatsClient.class)
@ComponentScan(basePackageClasses = StatsClientFallbackFactory.class)
public class StatsClientConfig {
}
