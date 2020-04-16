package io.todos.todosedge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Configuration;

@EnableZuulProxy
@SpringBootApplication
public class TodosEdgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodosEdgeApplication.class, args);
	}

}
