package com.dankan.config;

import com.amazonaws.services.s3.AmazonS3;
import com.dankan.repository.TokenRepository;
import com.dankan.repository.UnivRepository;
import com.dankan.repository.UserRepository;
import com.dankan.service.s3.S3UploadService;
import com.dankan.service.s3.S3UploaderServiceImpl;
import com.dankan.service.token.TokenService;
import com.dankan.service.token.TokenServiceImpl;
import com.dankan.service.univ.UnivService;
import com.dankan.service.univ.UnivServiceImpl;
import com.dankan.service.user.UserService;
import com.dankan.service.user.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AmazonS3 amazonS3Client;
    private final UnivRepository univRepository;

    public SpringConfig(final UserRepository userRepository, final AmazonS3 amazonS3Client, final TokenRepository tokenRepository, final UnivRepository univRepository) {
        this.userRepository = userRepository;
        this.amazonS3Client = amazonS3Client;
        this.tokenRepository = tokenRepository;
        this.univRepository = univRepository;
    }

    @Bean
    public UserService userService() {
        return new UserServiceImpl(userRepository,tokenRepository);
    }

    @Bean
    public S3UploadService S3UploadService() {
        return new S3UploaderServiceImpl(amazonS3Client);
    }

    @Bean
    public TokenService tokenService() {
        return new TokenServiceImpl(tokenRepository, userRepository);
    }

    @Bean
    public UnivService univService() {
        return new UnivServiceImpl(univRepository);
    }
}
