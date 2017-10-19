package me.stormma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WxpayApplication {

	private static Task task = new Task();

	public static void main(String[] args) {
		SpringApplication.run(WxpayApplication.class, args);

		int i = 0;
		while (true) {
			task.uploadImg();
			i++;
			System.out.println(i);
		}
	}

}
