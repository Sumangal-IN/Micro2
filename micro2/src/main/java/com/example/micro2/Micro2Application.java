package com.example.micro2;

import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RestController
@SpringBootApplication
@EnableCircuitBreaker
public class Micro2Application {

	public static void main(String[] args) {
		SpringApplication.run(Micro2Application.class, args);
	}

	@HystrixCommand(fallbackMethod = "supplyDefault")
	@RequestMapping(value = "/getMili", method = RequestMethod.GET)
	@ResponseBody
	public long getMili() {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Long> response = restTemplate.getForEntity("http://localhost:8003/getMili", Long.class);
		return response.getBody();
	}

	// I will break the circuit if within last 30 sec
	// (1) I have received at least 4 requests
	// (2) and found 75% requests are failed
	// (3) and will not retry for any further request for next 10 sec
	@HystrixCommand(fallbackMethod = "supplyDefault", commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "30000"),
			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "4"),
			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000") })
	@RequestMapping(value = "/getMili2", method = RequestMethod.GET)
	@ResponseBody
	public long getMili2() {
		System.out.println(new Date().getSeconds() + ": Trying to reach 8003");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Long> response = restTemplate.getForEntity("http://localhost:8003/getMili", Long.class);
		return response.getBody();
	}

	public long supplyDefault() {
		return 100000;
	}
}
