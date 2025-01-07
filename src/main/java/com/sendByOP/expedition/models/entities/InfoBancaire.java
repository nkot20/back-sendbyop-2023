package com.sendByOP.expedition.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;
import java.io.Serializable;

@AllArgsConstructor
@Data
@Entity
@Table(name = "info_bancaire")
public class InfoBancaire implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_info")
    private Integer idInfo;
    @JoinColumn(name = "id_client", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client idclient;

    @Column(name = "iban")
    private String iban;
    @Column(name = "country_name")
    private String countryname;
    @Column(name = "bank_account")
    private String bankAccount;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "bic")
    private String bic;

    @Column(name = "account_holder")
    private String accountHolder;

    public InfoBancaire() {
    }

    public InfoBancaire(Integer idInfo) {
        this.idInfo = idInfo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idInfo != null ? idInfo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InfoBancaire)) {
            return false;
        }
        InfoBancaire other = (InfoBancaire) object;
        if ((this.idInfo == null && other.idInfo != null) || (this.idInfo != null && !this.idInfo.equals(other.idInfo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.InfoBancaire[ idInfo=" + idInfo + " ]";
    }

}
