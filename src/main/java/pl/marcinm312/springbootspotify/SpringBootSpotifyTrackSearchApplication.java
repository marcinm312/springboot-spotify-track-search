package pl.marcinm312.springbootspotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SpringBootSpotifyTrackSearchApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringBootSpotifyTrackSearchApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSpotifyTrackSearchApplication.class, args);
	}

}
