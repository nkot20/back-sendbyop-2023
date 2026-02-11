package com.sendByOP.expedition.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendByOP.expedition.models.entities.Airport;
import com.sendByOP.expedition.models.entities.City;
import com.sendByOP.expedition.models.entities.Country;
import com.sendByOP.expedition.repositories.AirPortRepository;
import com.sendByOP.expedition.repositories.CityRepository;
import com.sendByOP.expedition.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Initialise les données de référence (pays, villes, aéroports) au démarrage de l'application
 * si la propriété app.data.init.airports est activée dans les properties
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final AirPortRepository airPortRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.data.init.airports:false}")
    private boolean initAirports;

    @Override
    @Transactional
    public void run(String... args) {
        if (!initAirports) {
            log.info("Airport initialization is disabled. Set app.data.init.airports=true to enable.");
            return;
        }

        log.info("Starting airport data initialization...");
        
        try {
            // Vérifier si les données existent déjà
            long airportCount = airPortRepository.count();
            if (airportCount > 0) {
                log.info("Airports already initialized ({} airports found). Skipping initialization.", airportCount);
                return;
            }

            loadAirportsFromJson();
            log.info("Airport data initialization completed successfully!");
            
        } catch (Exception e) {
            log.error("Error during airport data initialization", e);
            // Ne pas bloquer le démarrage de l'application
        }
    }

    /**
     * Charge les données des aéroports depuis le fichier JSON
     * Optimisé avec streams Java pour complexité O(n) au lieu de O(n³)
     */
    private void loadAirportsFromJson() throws IOException {
        log.info("Loading airports from JSON file...");
        
        ClassPathResource resource = new ClassPathResource("datas/airports-by-country.json");
        
        try (InputStream inputStream = resource.getInputStream()) {
            List<CountryData> countriesData = objectMapper.readValue(
                inputStream, 
                new TypeReference<List<CountryData>>() {}
            );
            
            // Charger toutes les données existantes en mémoire
            log.info("Loading existing data from database...");
            List<Country> existingCountries = countryRepository.findAll();
            List<City> existingCities = cityRepository.findAll();
            List<Airport> existingAirports = airPortRepository.findAll();
            
            // Créer des maps avec streams pour un accès O(1)
            java.util.Map<String, Country> countryMap = existingCountries.stream()
                .collect(java.util.stream.Collectors.toMap(Country::getName, c -> c));
            
            java.util.Map<String, City> cityMap = existingCities.stream()
                .collect(java.util.stream.Collectors.toMap(
                    city -> city.getName() + "_" + city.getCountry().getCountryId(),
                    city -> city
                ));
            
            java.util.Set<String> existingIataCodes = existingAirports.stream()
                .map(Airport::getIataCode)
                .collect(java.util.stream.Collectors.toSet());
            
            log.info("Existing data: {} countries, {} cities, {} airports", 
                existingCountries.size(), existingCities.size(), existingAirports.size());
            
            // Étape 1: Créer tous les pays manquants avec stream - O(n)
            List<Country> countriesToSave = countriesData.stream()
                .map(CountryData::getCountry)
                .filter(countryName -> !countryMap.containsKey(countryName))
                .map(countryName -> {
                    Country country = new Country();
                    country.setName(countryName);
                    return country;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Sauvegarder et mettre à jour la map
            if (!countriesToSave.isEmpty()) {
                List<Country> savedCountries = countryRepository.saveAll(countriesToSave);
                countryRepository.flush();
                savedCountries.forEach(c -> countryMap.put(c.getName(), c));
                log.info("Saved {} new countries", countriesToSave.size());
            }
            
            // Étape 2: Créer toutes les villes manquantes avec flatMap - O(n)
            List<City> citiesToSave = countriesData.stream()
                .flatMap(countryData -> {
                    Country country = countryMap.get(countryData.getCountry());
                    if (country == null) {
                        log.error("Country not found: {}", countryData.getCountry());
                        return java.util.stream.Stream.empty();
                    }
                    
                    return countryData.getCities().stream()
                        .filter(cityData -> {
                            String cityKey = cityData.getCity() + "_" + country.getCountryId();
                            return !cityMap.containsKey(cityKey);
                        })
                        .map(cityData -> City.builder()
                            .name(cityData.getCity())
                            .country(country)
                            .build());
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Sauvegarder et mettre à jour la map
            if (!citiesToSave.isEmpty()) {
                List<City> savedCities = cityRepository.saveAll(citiesToSave);
                cityRepository.flush();
                savedCities.forEach(city -> {
                    String key = city.getName() + "_" + city.getCountry().getCountryId();
                    cityMap.put(key, city);
                });
                log.info("Saved {} new cities", citiesToSave.size());
            }
            
            // Étape 3: Créer tous les aéroports manquants avec flatMap - O(n)
            List<Airport> airportsToSave = countriesData.stream()
                .flatMap(countryData -> {
                    Country country = countryMap.get(countryData.getCountry());
                    if (country == null) return java.util.stream.Stream.empty();
                    
                    return countryData.getCities().stream()
                        .flatMap(cityData -> {
                            String cityKey = cityData.getCity() + "_" + country.getCountryId();
                            City city = cityMap.get(cityKey);
                            if (city == null) return java.util.stream.Stream.empty();
                            
                            return cityData.getAirports().stream()
                                .filter(airportData -> !existingIataCodes.contains(airportData.getIataCode()))
                                .map(airportData -> Airport.builder()
                                    .name(airportData.getName())
                                    .iataCode(airportData.getIataCode())
                                    .city(city)
                                    .build());
                        });
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Sauvegarder les aéroports par batch de 100
            if (!airportsToSave.isEmpty()) {
                int batchSize = 100;
                java.util.stream.IntStream.range(0, (airportsToSave.size() + batchSize - 1) / batchSize)
                    .forEach(i -> {
                        int start = i * batchSize;
                        int end = Math.min(start + batchSize, airportsToSave.size());
                        List<Airport> batch = airportsToSave.subList(start, end);
                        airPortRepository.saveAll(batch);
                        airPortRepository.flush();
                        log.info("Saved batch of {} airports ({}/{})", batch.size(), end, airportsToSave.size());
                    });
            }
            
            log.info("Data initialization summary:");
            log.info("  - New countries: {}", countriesToSave.size());
            log.info("  - New cities: {}", citiesToSave.size());
            log.info("  - New airports: {}", airportsToSave.size());
            
        } catch (IOException e) {
            log.error("Failed to load airports from JSON file", e);
            throw e;
        }
    }

    /**
     * Classes internes pour le mapping JSON
     */
    private static class CountryData {
        private String country;
        private List<CityData> cities;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public List<CityData> getCities() {
            return cities;
        }

        public void setCities(List<CityData> cities) {
            this.cities = cities;
        }
    }

    private static class CityData {
        private String city;
        private List<AirportData> airports;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public List<AirportData> getAirports() {
            return airports;
        }

        public void setAirports(List<AirportData> airports) {
            this.airports = airports;
        }
    }

    private static class AirportData {
        private String name;
        private String iataCode;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIataCode() {
            return iataCode;
        }

        public void setIataCode(String iataCode) {
            this.iataCode = iataCode;
        }
    }
}
