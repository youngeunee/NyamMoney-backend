package com.ssafy.project.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ssafy.jwt")
public class JwtProperties {
	private String secret;
	private int accessExpmin;
	private int refreshExmin;
}
