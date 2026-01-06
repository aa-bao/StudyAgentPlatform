package org.example.kaoyanplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF
                .csrf(csrf -> csrf.disable())
                // 开启跨域允许
                .cors(cors -> cors.configure(http))
                // 配置权限控制
                .authorizeHttpRequests(auth -> auth
                        // 允许所有人访问登录、注册、以及上传后的静态资源路径
                        .requestMatchers("/user/login", "/user/register", "/uploads/**").permitAll()
                        // 允许访问Swagger/Knife4j文档
                        .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        // 暂时允许所有接口访问（方便你调试，等之后想做权限控制再改这里）
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}