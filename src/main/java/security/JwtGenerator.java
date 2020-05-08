package security;

// import com.ibm.websphere.security.jwt.Claims;
import com.ibm.websphere.security.jwt.InvalidBuilderException;
import com.ibm.websphere.security.jwt.InvalidClaimException;
import com.ibm.websphere.security.jwt.JwtBuilder;
import com.ibm.websphere.security.jwt.JwtException;
import com.ibm.websphere.security.jwt.KeyException;

public class JwtGenerator {

    public String getToken(String newUser, Long userId)
        throws JwtException, InvalidBuilderException, InvalidClaimException, KeyException {

        // 1. Create a JWTBuilder Object.
        JwtBuilder jb = JwtBuilder.create();

        // Overwrite any of the following
        // audience, expiration time, not before, subject, signing key or algorithm, jti
        jb.subject(newUser);
        jb.issuer("https://192.168.1.15:9443/jwt/defaultJWT");

        jb.claim("upn", newUser);
        jb.claim("groups", "users");
        jb.claim("id", userId);

        // 2. Create a JWT token
        return jb.buildJwt().compact();
    }

}
