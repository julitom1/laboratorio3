package edu.eci.userServiceApp.controller.auth;

import edu.eci.userServiceApp.exception.InvalidCredentialsException;
import edu.eci.userServiceApp.repository.document.RoleEnum;
import edu.eci.userServiceApp.repository.document.User;
import edu.eci.userServiceApp.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping( "v1/auth" )
public class AuthController {

    @Value("${app.secret}")
    String secret;

    private final UserService userService;

    public AuthController(@Autowired UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public TokenDto login(@RequestBody LoginDto loginDto) {
        User user = userService.findByEmail(loginDto.email);
        if (BCrypt.checkpw(loginDto.password, user.getPasswordHash())) {
            return generateTokenDto(user);
        } else {
            throw new InvalidCredentialsException();
        }

    }

    private String generateToken(User user, Date expirationDate) {
        return Jwts.builder()
                .setSubject(user.getId())
                .claim(String.valueOf(RoleEnum.ADMIN), user.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    private TokenDto generateTokenDto(User user) {
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.add(Calendar.MINUTE, 5);
        String token = generateToken(user, expirationDate.getTime());
        return new TokenDto(token, expirationDate.getTime());
    }
}
