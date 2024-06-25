package com.kmkbe.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;*/

@Configuration
//@EnableSwagger2
public class SwaggerConfig {

/*    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder().title("Kmk Digital 2.0")
                .description("Kmk Digital 2.0 API Documentation")
                //.termsOfServiceUrl("http://adenurhidayat.com")
                .contact(getContact()).license("kmk License")
                //.licenseUrl("ade.enhaa@gmail.com")
                .version("1.0")
                .build();
    }

    private Contact getContact() {
        return new Contact(
                "Khesa NikitaGenerator",
                "https://nikitagenerator.com",
                "khesa@nikitagenerator.com"
        );
    }*/
}
