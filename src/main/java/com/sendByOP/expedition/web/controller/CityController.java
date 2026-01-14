package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.models.dto.CityDto;
import com.sendByOP.expedition.services.iServices.ICityService;
import com.sendByOP.expedition.services.impl.CityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/cities")
@RequiredArgsConstructor
public class CityController {

    private final ICityService cityService;
    @GetMapping
    public List<CityDto> getAllCities() {
        return cityService.getAllCities();
    }

    @GetMapping("/{id}")
    public CityDto getCityById(@PathVariable Integer id) {
        return cityService.getCityById(id);
    }

    @PostMapping
    public CityDto createCity(@RequestBody CityDto cityDTO) {
        return cityService.createCity(cityDTO);
    }

    @PutMapping("/{id}")
    public CityDto updateCity(@PathVariable Integer id, @RequestBody CityDto cityDTO) {
        return cityService.updateCity(id, cityDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteCity(@PathVariable Integer id) {
        cityService.deleteCity(id);
    }
}
