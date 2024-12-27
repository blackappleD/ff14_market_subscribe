package com.ff14.market;

import com.ff14.market.po.FF14ItemPO;
import com.ff14.market.repo.FF14ItemRepo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackageClasses = FF14ItemPO.class)
@EnableJpaRepositories(basePackageClasses = FF14ItemRepo.class)
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(
				Application.class
		).run(args);
	}
}