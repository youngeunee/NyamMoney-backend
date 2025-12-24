package com.ssafy.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.ssafy.project.api.v1.challenge.chat.interceptor.ChatConnectInterceptor;
import com.ssafy.project.api.v1.challenge.chat.interceptor.ChatSubscribeInterceptor;
import com.ssafy.project.api.v1.challenge.chat.interceptor.JwtHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
	private final ChatSubscribeInterceptor chatSubscribeInterceptor;
	private final ChatConnectInterceptor chatConnectInterceptor;
	public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor, ChatSubscribeInterceptor chatSubscribeInterceptor, ChatConnectInterceptor chatConnectInterceptor) {
		this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
		this.chatSubscribeInterceptor = chatSubscribeInterceptor;
		this.chatConnectInterceptor = chatConnectInterceptor;
	}
    /**
     * 메시지 브로커 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독(subscribe)할 주소 prefix
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 메시지 보낼 때 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 웹소켓 연결 엔드포인트
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-challenge-chat")
                .setAllowedOriginPatterns("*");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
        		chatConnectInterceptor,
        		chatSubscribeInterceptor
        		);
    }


}
