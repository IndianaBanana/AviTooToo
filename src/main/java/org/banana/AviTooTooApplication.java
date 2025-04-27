package org.banana;

import org.banana.dto.user.UserDto;
import org.banana.entity.Comment;
import org.banana.entity.User;
import org.banana.repository.CommentRepository;
import org.banana.security.service.AuthService;
import org.banana.service.CommentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootApplication
public class AviTooTooApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AviTooTooApplication.class, args);
        CommentService bean = run.getBean(CommentService.class);
        bean.printComments();
    }

}
