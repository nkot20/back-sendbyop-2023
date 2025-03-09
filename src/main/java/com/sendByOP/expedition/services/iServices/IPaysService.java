package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.entities.Country;

import java.util.List;

public interface IPaysService {
    Country saveCountry(Country pays);
    List<Country> getCountry();

}
