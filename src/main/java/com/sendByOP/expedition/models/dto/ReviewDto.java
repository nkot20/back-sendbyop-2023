package com.sendByOP.expedition.models.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ReviewDto {
    private int id;
    private String rating;
    private String opinion;
    private Date date;
    private Integer transporterId;
    private Integer shipperId;
}