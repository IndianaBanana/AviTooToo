package org.banana;

import org.banana.dto.user.UserLoginRequestDto;
import org.banana.dto.user.UserRegisterRequestDto;
import org.banana.security.service.AuthService;
import org.banana.service.CommentService;
import org.banana.service.CommentServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
//        AuthService bean = run.getBean(AuthService.class);
//        bean.register(new UserRegisterRequestDto(
//                "user4",
//                "user4",
//                "user4",
//                "user4@user4.user",
//                "user4",
//                "user4"
//        ));
//        System.out.println(bean.verify(new UserLoginRequestDto(
//                "user3@user3.user",
//                "user3"
//        )));
    }

}
