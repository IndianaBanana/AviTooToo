package org.banana;

import org.banana.security.dto.UserLoginRequestDto;
import org.banana.security.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
        UserService bean = run.getBean(UserService.class);
//        bean.register(new UserRegisterRequestDto(
//                "user",
//                "user",
//                "user",
//                "user@user.user",
//                "user",
//                "user"
//        ));
//        PasswordEncoder bean1 = run.getBean(PasswordEncoder.class);
//        System.out.println(bean1.encode("user"));
        System.out.println(bean.verify(new UserLoginRequestDto(
                "user@user.user",
                "user"
        )));
    }
}
