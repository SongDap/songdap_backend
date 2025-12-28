package com.nodap.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger (OpenAPI) 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("https://api.nodap.com").description("프로덕션 서버")
                ))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("accessToken")
                                .description("Access Token (쿠키)")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
    }

    private Info apiInfo() {
        return new Info()
                .title("NoDap API")
                .description("""
                        ## NoDap 백엔드 API 문서
                        
                        ### 인증 방식
                        - 모든 인증은 **쿠키 기반**으로 처리됩니다.
                        - 로그인 성공 시 `accessToken`, `refreshToken` 쿠키가 설정됩니다.
                        - 쿠키 속성: `HttpOnly; Secure; SameSite=None`
                        
                        ### 토큰 갱신
                        - Access Token 만료 시 `/api/v1/auth/reissue` 호출
                        - Refresh Token도 만료된 경우 재로그인 필요
                        
                        ### 에러 응답 형식
                        ```json
                        {
                          "code": 401,
                          "errorCode": "AUTH_002",
                          "message": "유효하지 않은 인가 코드입니다.",
                          "timestamp": "2025-12-28T12:00:00"
                        }
                        ```
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("NoDap Team")
                        .email("support@nodap.com")
                );
    }
}



