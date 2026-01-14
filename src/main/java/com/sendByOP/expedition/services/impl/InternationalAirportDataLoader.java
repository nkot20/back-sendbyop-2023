package com.sendByOP.expedition.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendByOP.expedition.models.dto.AirportDto;
import com.sendByOP.expedition.models.dto.CityDto;
import com.sendByOP.expedition.models.dto.CountryDto;
import com.sendByOP.expedition.services.iServices.ICityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InternationalAirportDataLoader {

    private final CountryService countryService;
    private final ICityService cityService;
    private final AirportService airportService;
    private final ObjectMapper objectMapper;

    /*@EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadInternationalAirports() throws Exception {
        // Charge le JSON des aéroports internationaux
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("international-airports.json");
        ) {
            if (is == null) {
                throw new IllegalStateException("Le fichier international-airports.json est introuvable");
            }

            List<CountryJson> countries = objectMapper.readValue(is, new TypeReference<>() {});

            for (CountryJson countryJson : countries) {
                // Enregistre le pays
                CountryDto countryDto = CountryDto.builder()
                        .name(countryJson.getCountry())
                        .build();
                countryDto = countryService.saveCountry(countryDto);

                for (CityJson cityJson : countryJson.getCities()) {
                    // Enregistre la ville
                    CityDto cityDto = CityDto.builder()
                            .name(cityJson.getCity())
                            .countryId(countryDto.getCountryId())
                            .build();
                    cityDto = cityService.createCity(cityDto);

                    for (AirportJson airportJson : cityJson.getAirports()) {
                        // Enregistre l'aéroport
                        AirportDto airportDto = AirportDto.builder()
                                .name(airportJson.getName())
                                .cityId(cityDto.getCityId())
                                .iataCode(airportJson.getIataCode())
                                .createdBy("system")
                                .updatedBy("system")
                                .build();
                        airportService.saveAeroPort(airportDto);
                    }
                }
            }
        }
    }*/

    // Classes internes pour désérialisation
    private static class CountryJson {
        private String country;
        private List<CityJson> cities;
        // getters & setters
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public List<CityJson> getCities() { return cities; }
        public void setCities(List<CityJson> cities) { this.cities = cities; }
    }

    private static class CityJson {
        private String city;
        private List<AirportJson> airports;
        // getters & setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public List<AirportJson> getAirports() { return airports; }
        public void setAirports(List<AirportJson> airports) { this.airports = airports; }
    }

    private static class AirportJson {
        private String name;
        private String iataCode;
        // getters & setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIataCode() { return iataCode; }
        public void setIataCode(String iataCode) { this.iataCode = iataCode; }
    }
}
