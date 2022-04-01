/*
package gr.uaegean.location.emulation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver(@Value("${multipart.max.file.size.mb:#{null}}") Optional<Double> declaredFileSizeLimitMB) {
        long maxUploadSize = 20 * 1024 * 1024L; // default max upload size per file is 20mb
        if (declaredFileSizeLimitMB.isPresent()) {
            maxUploadSize = -1;
        }
        final CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSizePerFile(maxUploadSize);
        resolver.setMaxInMemorySize(Integer.MAX_VALUE);
        resolver.setDefaultEncoding("UTF-8");
        resolver.setResolveLazily(true);
        return resolver;
    }
}
*/
