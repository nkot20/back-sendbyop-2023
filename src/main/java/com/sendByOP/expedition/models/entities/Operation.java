package com.sendByOP.expedition.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "operation")
@Builder
public class Operation extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "operation_id")
    private Integer id;

    @Column(name = "operation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date operationDate;

    @JoinColumn(name = "operation_type_id", referencedColumnName = "id_type_operation")
    @ManyToOne(optional = false)
    private OperationType operationType;

    @JoinColumn(name = "reservation_id", referencedColumnName = "id_re")
    @ManyToOne(optional = false)
    private Booking reservation;;
}
