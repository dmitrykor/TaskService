package org.dmitry.tasks;

import org.dmitry.tasks.repo.UserRepository;
import org.dmitry.tasks.resources.ELinkRelation;
import org.dmitry.tasks.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	UserRepository userRepo;

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.authorizeRequests()
				.antMatchers("/api/" + ELinkRelation.TASKS.name().toLowerCase() + "/**").hasRole(Constants.MANAGER_ROLE)
				.antMatchers("/api/" + ELinkRelation.INBOX.name().toLowerCase() + "/**").hasAnyRole(Constants.MANAGER_ROLE, Constants.USER_ROLE)
	            .antMatchers("/api").hasAnyRole(Constants.MANAGER_ROLE, Constants.USER_ROLE).and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.httpBasic();
	}

	@Bean
	protected UserDetailsService userDetailsService() {
		return (username) -> userRepo.findByUsername(username)
				.map(a -> new User(a.getUsername(), a.getPassword(), true, true, true, true,
						AuthorityUtils.commaSeparatedStringToAuthorityList(a.getRoles())))
				.orElseThrow(() -> new UsernameNotFoundException("Could not find the user '" + username + "'."));
	}
}
