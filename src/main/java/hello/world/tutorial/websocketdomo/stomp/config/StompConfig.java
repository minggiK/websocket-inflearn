package hello.world.tutorial.websocketdomo.stomp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        WebSocketMessageBrokerConfigurer.super.registerStompEndpoints(registry);
        registry.addEndpoint("/helloworld").withSockJS(); // 서버와 클라이언트의 엔드포인트를 똑같이 설정
        //withSockJS : 웹소켓이 안될때 서버쪽에서 sockJS 사용하도록 설정
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
//        WebSocketMessageBrokerConfigurer.super.configureMessageBroker(config);
    //단순 prefix : 클라이언트끼리 pub -> sub 만 가능
    config.enableSimpleBroker("/topic", "/queue");
    //SpringBoot의 java로 prefix 실행 -> "/app/~~" (Controller -> @MessageMapping("/hello"))
    config.setApplicationDestinationPrefixes("/app");
    }
}
