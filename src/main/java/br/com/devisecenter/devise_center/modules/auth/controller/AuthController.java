package br.com.devisecenter.devise_center.modules.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.devisecenter.devise_center.modules.auth.dtos.LoginDTO;
import br.com.devisecenter.devise_center.modules.auth.dtos.RegisterDTO;
import br.com.devisecenter.devise_center.modules.auth.service.TokenService;
import br.com.devisecenter.devise_center.modules.users.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private UserService userService;

    private AuthenticationManager authManager;

    private TokenService tokenService;

    public AuthController(UserService userService, AuthenticationManager authManager, TokenService tokenService) {
        this.userService = userService;
        this.authManager = authManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO login) {

        var userandpassword = new UsernamePasswordAuthenticationToken(login.username(), login.password());

        authManager.authenticate(userandpassword);

        return ResponseEntity.ok(tokenService.generateToken(login.username()));
    }

    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody RegisterDTO userDTO) {
        userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
