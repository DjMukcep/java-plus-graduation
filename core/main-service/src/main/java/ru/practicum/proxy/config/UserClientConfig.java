package ru.practicum.proxy.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.practicum.proxy.user.UserClient;
import ru.practicum.proxy.user.UserClientFallbackFactory;

@Configuration
@EnableFeignClients(clients = UserClient.class)
@ComponentScan(basePackageClasses = UserClientFallbackFactory.class)
public class UserClientConfig {
}
