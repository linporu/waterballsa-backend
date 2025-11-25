package waterballsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WaterballsaBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(WaterballsaBackendApplication.class, args);
  }
}
