package ru.practicum.internal.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.practicum.internal.request.RequestClient;
import ru.practicum.internal.request.RequestClientFallbackFactory;
import ru.practicum.internal.user.UserClient;
import ru.practicum.internal.user.UserClientFallbackFactory;

@Configuration
@EnableFeignClients(clients = {UserClient.class, RequestClient.class})
@ComponentScan(basePackageClasses = {UserClientFallbackFactory.class, RequestClientFallbackFactory.class})
public class UserClientConfig {
}
