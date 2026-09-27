//package com.nttho.orderservice.configs;
//
//import com.fasterxml.jackson.databind.JsonDeserializer;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.core.KafkaOperations;
//import org.springframework.kafka.listener.CommonErrorHandler;
//import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
//import org.springframework.kafka.listener.DefaultErrorHandler;
//import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
//import org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper;
//import org.springframework.kafka.support.mapping.JacksonJavaTypeMapper;
//import org.springframework.util.backoff.BackOff;
//import org.springframework.util.backoff.FixedBackOff;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class KafkaConsumerConfig {
//
//    @Bean
//    public ConsumerFactory<String, Object> consumerFactory(){
//        Map<String, Object> config = new HashMap<>();
//
//        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
//        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//
//        return new DefaultKafkaConsumerFactory<>(config);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
//            CommonErrorHandler commonErrorHandler
//    ){
//        // mục đích: ConcurrentKafkaListenerContainerFactory
//        /*
//        @KafkaListener
//              ↓
//        ConcurrentKafkaListenerContainerFactory
//              ↓
//        Kafka Consumer
//              ↓
//        Nhận message
//              ↓
//        Gọi method của bạn
//         */
//
//        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        factory.setCommonErrorHandler(commonErrorHandler);
//
//        StringJacksonJsonMessageConverter converter = new StringJacksonJsonMessageConverter();
//        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
//        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
//
//        converter.setTypeMapper(typeMapper);
//        factory.setRecordMessageConverter(converter);
//
//        return factory;
//    }
//
//    @Bean
//    public CommonErrorHandler commonErrorHandler(KafkaOperations<String, Object> template){
//        BackOff backOff = new FixedBackOff(2000L, 3);
//        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
//
//        return new DefaultErrorHandler(recoverer, backOff);
//    }
//}