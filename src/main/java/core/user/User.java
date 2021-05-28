package core.user;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.ibm.websphere.security.jwt.InvalidBuilderException;
import com.ibm.websphere.security.jwt.InvalidClaimException;
import com.ibm.websphere.security.jwt.JwtException;
import com.ibm.websphere.security.jwt.KeyException;

import security.JwtGenerator;

@Entity(name = "User")
@Table(name = "USER_TABLE")
public class User extends AbstractUser {

    public String getToken() throws JwtException, InvalidBuilderException, InvalidClaimException, KeyException {
        return JwtGenerator.getToken(this.getUsername(), this.getUserID());
    }

}
