package com.fleetmaster.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("csv", MediaType.parseMediaType("text/csv"));
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Ensure JSON converter exists
        boolean hasJsonConverter = converters.stream()
            .anyMatch(c -> c instanceof MappingJackson2HttpMessageConverter);
        
        if (!hasJsonConverter) {
            converters.add(new MappingJackson2HttpMessageConverter());
        }

        // Ensure XML converter exists
        boolean hasXmlConverter = converters.stream()
            .anyMatch(c -> c instanceof MappingJackson2XmlHttpMessageConverter);
        
        if (!hasXmlConverter) {
            converters.add(new MappingJackson2XmlHttpMessageConverter());
        }

        // Add CSV converter
        converters.add(new CsvHttpMessageConverter());
    }
}
