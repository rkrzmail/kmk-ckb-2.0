package com.kmkbe.modules.apis;


import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.service.JwtValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class JwtValidatorController {

    @Autowired
    private JwtValidatorService jwtValidatorService;

    /**
     * Endpoint validasi JWT
     * @param jwtToken  - JWT token di path URL
     * @param apiKey    - app_key dari header request
     */
    @PostMapping("/validate/{jwtToken}")
    public ResponseEntity<CommonResult<ValidationResponse>> validateToken(
            @PathVariable("jwtToken") String jwtToken,
            @RequestHeader("ApiKey") String apiKey) {

        CommonResult<ValidationResponse> result = new CommonResult<>();

        try {
            // Panggil service untuk validasi
            ValidationResponse validationResponse = jwtValidatorService.validate(apiKey, jwtToken);

            result.setMessage("SUCCESS");
            result.setCode(200);
            result.setMessage("Token valid");
            result.setData(validationResponse);

            return ResponseEntity.ok(result);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            result.setMessage("FAILED");
            result.setCode(401);
            result.setMessage("Token expired: " + e.getMessage());
            return ResponseEntity.status(401).body(result);

        } catch (io.jsonwebtoken.SignatureException e) {
            result.setMessage("FAILED");
            result.setCode(401);
            result.setMessage("Invalid token signature");
            return ResponseEntity.status(401).body(result);

        } catch (IllegalArgumentException e) {
            result.setMessage("FAILED");
            result.setCode(401);
            result.setMessage(e.getMessage());
            return ResponseEntity.status(401).body(result);

        } catch (Exception e) {
            result.setMessage("FAILED");
            result.setCode(500);
            result.setMessage("Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}


