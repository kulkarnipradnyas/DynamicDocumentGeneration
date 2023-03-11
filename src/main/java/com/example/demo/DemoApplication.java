package com.example.demo;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Configuration
@EnableAsync //The @EnableAsync annotation switches on Spring's ability to run @Async methods in a background thread pool.
@EnableScheduling //The @EnableScheduling annotation is used to enable the scheduler for your application.
@EnableTransactionManagement //activated (or configured transaction handling some other way).
@EnableRetry //The @EnableRetry annotation enables the spring retry feature in the application.
@SpringBootApplication( // @Configuration + @ComponentScan + EnableAutoConfiguration
scanBasePackages = {
		"com.example.demo",
		})
public class DemoApplication {

//	@Bean(name = "destroy")
//	public void getAsyncExecutor() {
//
//		try{
//			InetAddress addr = InetAddress.getByName("google.com" );
//			System.out.println("i am gone addr" + addr);
//
//		}catch(IOException e){
//			System.out.println("i am gone");
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}


	// every day 12am clean this
	@Scheduled(cron="0 0 0 * * ?")
	public void cleanFolder() throws Exception {
		Path mydocsPath = Paths.get("mydoc");
		if (Files.exists(mydocsPath)) {
			FileUtils.deleteDirectory(mydocsPath.toFile());
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
