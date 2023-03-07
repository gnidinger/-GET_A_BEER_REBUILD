package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class GetabeerRebuildApplication {

	public static void main(String[] args) {
		SpringApplication.run(GetabeerRebuildApplication.class, args);
	}

}
