package com.acme.audit.demo;
import org.springframework.context.annotation.Configuration;import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
@Configuration @EnableWebSecurity public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
 @Override protected void configure(AuthenticationManagerBuilder auth)throws Exception{auth.inMemoryAuthentication().withUser("U10001").password("{noop}demo-only-password").roles("CUSTOMER_SERVICE");}
 @Override protected void configure(HttpSecurity http)throws Exception{http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();}
}
