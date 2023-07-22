package com.sendByOP.expedition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ville")
public class Ville implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idville")
    private Integer idville;

    @Column(name = "nom")
    private String nom;

  /*  @OneToMany(cascade = CascadeType.ALL, mappedBy = "idVille")
    private List<Aeroport> aeroportList;*/

    @JoinColumn(name = "idpays", referencedColumnName = "idpays")
    @ManyToOne(optional = false)
    private Pays idpays;

    public Ville() {
    }

    public Ville(Integer idville) {
        this.idville = idville;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idville != null ? idville.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ville)) {
            return false;
        }
        Ville other = (Ville) object;
        if ((this.idville == null && other.idville != null) || (this.idville != null && !this.idville.equals(other.idville))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Ville[ idville=" + idville + " ]";
    }

}
