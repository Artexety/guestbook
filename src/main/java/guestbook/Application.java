/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guestbook;

import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import java.util.Comparator; // for Comparator
import java.io.File;         // for File


import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * The core class to bootstrap our application. It triggers Spring Boot's auto-configuration, component scanning and
 * configuration properties scanning using the {@link SpringBootApplication} convenience annotation. At the same time,
 * this class acts as configuration class to configure additional components (see {@link #init(GuestbookRepository)})
 * that the Spring container will take into account when bootstrapping.
 *
 * @author Paul Henke
 * @author Oliver Drotbohm
 */
@SpringBootApplication
public class Application {

	/**
	 * The main application method, bootstraps the Spring container.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Some initializing code to populate our database with some {@link GuestbookEntry}s. Beans of type
	 * {@link CommandLineRunner} will be executed on application startup which makes them a convenient way to run
	 * initialization code.
	 */
	@Bean
	CommandLineRunner init(GuestbookRepository guestbook) {
		return args -> {

			Stream.of( //
					new GuestbookEntry("H4xx0r", "first!!!", "test@mail.com", null), //
					new GuestbookEntry("Arni", "Hasta la vista, baby", "test@mail.com", null), //
					new GuestbookEntry("Duke Nukem",
							"It's time to kick ass and chew bubble gum. And I'm all out of gum.", "test@mail.com", null), //
					new GuestbookEntry("Gump1337",
							"Mama always said life was like a box of chocolates. You never know what you're gonna get.", "test@mail.com", null)) //
					.forEach(guestbook::save);
		};
	}

    @PostConstruct
    public void onStartUp() {
        File uploadDir = new File("uploads/");
        if (!uploadDir.exists()) {
            uploadDir.mkdir(); // create the directory if it doesn't exist
        }
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("Server is shutting down. Clearing uploads directory...");
        clearUploadsDirectory();
    }


    public void clearUploadsDirectory() {
        try {
            Path uploadsDir = Paths.get("uploads/");

            if (Files.exists(uploadsDir)) {
                Files.walk(uploadsDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

                System.out.println("Uploads directory cleared!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred while clearing uploads directory.");
        }
    }

	/**
	 * This class customizes the web and web security configuration through callback methods provided by the
	 * {@link WebMvcConfigurer} interface.
	 */
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class SecurityConfiguration implements WebMvcConfigurer {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry)
		 */
		@Override
		public void addViewControllers(ViewControllerRegistry registry) {

			// Route requests to /login to the login view (a default one provided by Spring Security)
			registry.addViewController("/login").setViewName("login");
		}

		@Bean
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

			http.csrf().disable();

			// Allow all requests on the URI level, configure form login.
			http.authorizeRequests().anyRequest().permitAll() //
					.and().formLogin() //
					.and().logout().logoutSuccessUrl("/").clearAuthentication(true);

			return http.build();
		}

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:uploads/"); 
        }
	}
}
