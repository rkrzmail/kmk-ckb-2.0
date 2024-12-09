package com.kmkbe.core.config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kmkbe.core.converter.ObjectToUrlEncodedConverter;
import com.kmkbe.nikita.utils.PlainDoubleSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configure ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        objectMapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);


        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new PlainDoubleSerializer());
        module.addSerializer(double.class, new PlainDoubleSerializer());
        objectMapper.registerModule(module);


        // Set custom ObjectMapper to RestTemplate
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        restTemplate.getMessageConverters().add( messageConverter);
        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));



        return restTemplate;
    }

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Daftarkan serializer global untuk tipe Double
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new PlainDoubleSerializer());
        module.addSerializer(double.class, new PlainDoubleSerializer());
        mapper.registerModule(module);

        Example example = new Example(1234567.000000123456, 0.000000123456);
        System.out.println(mapper.writeValueAsString(example));


        //  mapper = new ObjectMapper().enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
        //module = new SimpleModule();
       // module.addSerializer(Double.class, new PlainDoubleSerializer());
        mapper.registerModule(module);
          Map<String, Double > m = new HashMap<>();
        m.put("test",  (0.0000000005));
        m.put("test1",  (0.000000123456));
       // System.out.println(mapper.valueToTree(m).toString());
        System.out.println(mapper.writeValueAsString(m) );
    }

    static class Example {
        private double value1;
        private double value2;

        public Example(double value1, double value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public double getValue1() {
            return value1;
        }

        public double getValue2() {
            return value2;
        }
    }
}