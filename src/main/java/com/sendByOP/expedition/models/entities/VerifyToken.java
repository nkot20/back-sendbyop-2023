package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "verify_token")
public class VerifyToken extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tokenid")
    int tokenid;

    @Column(name = "token")
    String token;

    @Column(name = "expirated_token")
    @Temporal(TemporalType.TIMESTAMP)
    Date expiratedToken;

    @Basic(optional = false)
    @Column(name = "email")
    String email;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerifyToken that = (VerifyToken) o;
        return tokenid == that.tokenid && Objects.equals(token, that.token) && Objects.equals(expiratedToken, that.expiratedToken) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenid, token, expiratedToken, email);
    }
}
