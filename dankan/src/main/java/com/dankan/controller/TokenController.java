package com.dankan.controller;

import com.dankan.dto.response.login.TokenResponseDto;
import com.dankan.dto.resquest.token.TokenRequestDto;
import com.dankan.service.token.TokenService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@AllArgsConstructor
public class TokenController {
    private final TokenService tokenService;

    @GetMapping("check")
    @ApiOperation(value = "토큰이 만료됐는지 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답"),
    })
    public ResponseEntity<Boolean> isExpiredAt() {
        return ResponseEntity.ok(tokenService.isExpired());
    }

    @PostMapping("reissue")
    @ApiOperation(value = "토큰 재발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답"),
    })
    public ResponseEntity<TokenResponseDto> reissueAccessToken(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseEntity.ok(tokenService.reissueAccessToken(tokenRequestDto));
    }
}
