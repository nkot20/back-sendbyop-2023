package com.sendByOP.expedition.services.iServices;

import com.sendByOP.expedition.models.entities.Visite;

public interface IVisiteService {
    Visite addVisitor(Visite visite);
    int getVisitorCount();
}