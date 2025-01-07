package com.sendByOP.expedition.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "note")
public class Note implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_note")
    private Integer idNote;

    @Basic(optional = false)
    @Column(name = "nb")
    private int nb;

    @JoinColumn(name = "id_expe", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client idexp;

    @JoinColumn(name = "id_client", referencedColumnName = "idp")
    @ManyToOne(optional = false)
    private Client idClient;

    public Note() {
    }

    public Note(Integer idNote) {
        this.idNote = idNote;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idNote != null ? idNote.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Note)) {
            return false;
        }
        Note other = (Note) object;
        if ((this.idNote == null && other.idNote != null) || (this.idNote != null && !this.idNote.equals(other.idNote))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.sendByOP.sendByOp.model.Note[ idNote=" + idNote + " ]";
    }
}
