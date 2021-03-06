package com.fean.seufinanceiro.controllers;

import com.fean.seufinanceiro.dtos.SignUpDto;
import com.fean.seufinanceiro.dtos.UserDto;
import com.fean.seufinanceiro.exceptions.UserNotFoundException;
import com.fean.seufinanceiro.models.User;
import com.fean.seufinanceiro.responses.Response;
import com.fean.seufinanceiro.security.JwtUser;
import com.fean.seufinanceiro.security.dtos.TokenDto;
import com.fean.seufinanceiro.security.enums.ProfileEnum;
import com.fean.seufinanceiro.security.utils.JwtTokenUtil;
import com.fean.seufinanceiro.services.UserService;
import com.fean.seufinanceiro.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    public UserController(UserService userService, JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    public ResponseEntity<Response<UserDto>> getUser(@AuthenticationPrincipal JwtUser jwtUser) {
        LOGGER.info("Searching User data by ID: ", jwtUser.getId());

        Response<UserDto> response = new Response<>();
        Optional<User> user = userService.findUsuarioById(jwtUser.getId());

        if (!user.isPresent()) {
            LOGGER.info("User not found by ID: ", jwtUser.getId());
            response.getErrors().add("User not found by ID: " + jwtUser.getId());
            return ResponseEntity.badRequest().body(response);
        }

        response.setData(this.convertUserDto(user.get()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Response<TokenDto>> save(@Valid @RequestBody SignUpDto signUpDto,
                                                   BindingResult result) {

        Response<TokenDto> response = new Response<>();

        checkUserSignUpData(signUpDto, result);
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(response);
        }

        User user = userService.newUser(convertNewUserDto(signUpDto));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenUtil.obtainToken(userDetails);

        response.setData(new TokenDto(token));

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<String>> update(@Valid @RequestBody UserDto userDto,
                                                   @AuthenticationPrincipal JwtUser jwtUser,
                                                   BindingResult result) {

        LOGGER.info("Updating user: {}", userDto.toString());
        Response<String> response = new Response<>();

        if (result.hasErrors()) {
            LOGGER.error("Erro validando usuário: {}", result.getAllErrors());
            result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(response);
        }

        this.userService.newUser(convertUser(userDto, jwtUser));
        response.setData("Usuário atualizado com sucesso!!!");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Response<String>> remove(@AuthenticationPrincipal JwtUser jwtUser) {

        LOGGER.info("Removing user ID: {}", jwtUser.getId());

        Response<String> response = new Response<>();
        Optional<User> user = this.userService.findUsuarioById(jwtUser.getId());

        if (user == null) {
            LOGGER.info("Error removing user ID: {} is invalid", jwtUser.getId());
            response.getErrors().add("Error removing user. Record not found by id " + jwtUser.getId());
            return ResponseEntity.badRequest().body(response);
        }

        this.userService.removeUser(jwtUser.getId());

        response.setData("User removed successfully!");
        return ResponseEntity.ok(response);
    }

    private void checkUserSignUpData(SignUpDto signUpDto, BindingResult result) {

        LOGGER.info("Validating user ID {}: ", signUpDto.getEmail());

        Optional<User> usuario = this.userService
                .findUserByUsernameEmail(signUpDto.getEmail());

        if (usuario.isPresent()) {
            result.addError(new ObjectError("User",
                    "User already registered"));
        }
    }

    private User convertNewUserDto(SignUpDto signUpDto) {
        User user = new User();
        user.setName(signUpDto.getName());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(PasswordUtils.generateBCrypt(signUpDto.getPassword()));
        user.setProfile(ProfileEnum.ROLE_USER);
        return user;
    }

    private UserDto convertUserDto(User user) {
        return new UserDto(String.valueOf(user.getId()),
                user.getName(),
                user.getEmail());
    }

    private User convertUser(UserDto userDto, JwtUser jwtUser) {
        Optional<User> user = userService.findUsuarioById(jwtUser.getId());

        if (!user.isPresent()) {
            throw new UserNotFoundException("User not found!");
        }

        if (userDto.getPassword() != null) {
            if (!userDto.getPassword().isEmpty()) {
                user.get().setPassword(PasswordUtils.generateBCrypt(userDto.getPassword()));
            }
        }

        user.get().setId(jwtUser.getId());
        user.get().setName(userDto.getName());
        user.get().setEmail(userDto.getEmail());
        user.get().setProfile(ProfileEnum.ROLE_USER);

        return user.get();
    }

}
