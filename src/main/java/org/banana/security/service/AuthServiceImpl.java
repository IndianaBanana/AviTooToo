//package org.banana.security.service;
//
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.banana.dto.UserDto;
//import org.banana.entity.User;
//import org.banana.exception.InvalidUserAddingEmailException;
//import org.banana.exception.InvalidUserAddingPhoneException;
//import org.banana.repository.UserRepository;
//import org.banana.security.UserRole;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Slf4j
//@Service
//public class AuthServiceImpl implements AuthService {
//
//    private final JwtService jwtService;
//    private final AuthenticationManager authManager;
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public AuthServiceImpl(JwtService jwtService, AuthenticationManager authManager, UserRepository userRepository,
//                           PasswordEncoder passwordEncoder) {
//        this.jwtService = jwtService;
//        this.authManager = authManager;
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    @Transactional
//    public UserDto register(UserDto userDto) {
//        log.debug("entering register method in {}", this.getClass().getSimpleName());
//        if (userRepository.existsByUsername(userDto.getUsername())) {
//            throw new InvalidUserAddingEmailException(userDto.getUsername());
//        }
//        if (userRepository.existsByPhone(userDto.getPhoneNumber())) {
//            throw new InvalidUserAddingPhoneException(userDto.getPhoneNumber());
//        }
//        String clientId = UUID.randomUUID().toString();
//        String password = passwordEncoder.encode(userDto.getPassword());
//        userDto.setId(clientId);
//        User user = new User(clientId,
//                userDto.getFirstName(),
//                userDto.getLastName(),
//                userDto.getAddress(),
//                userDto.getUsername(),
//                password,
//                UserRole.ROLE_USER,
//                userDto.getPhoneNumber());
//
//        userRepository.add(user);
//        userDto.setPassword(null);
//        return userDto;
//    }
//
//    @Override
//    public String verify(UserDto userDto) {
//        Authentication authentication = authManager
//                .authenticate(new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword()));
//        if (authentication != null && authentication.isAuthenticated()) {
//            return jwtService.generateToken(userDto.getUsername());
//        } else {
//            return "failed to verify user";
//        }
//    }
//}
