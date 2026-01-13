package com.fleetmaster.services;

import com.fleetmaster.entities.Info;
import com.fleetmaster.repositories.InfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InfoService {
    private final InfoRepository infoRepository;
    public InfoService(InfoRepository infoRepository) {
        this.infoRepository = infoRepository;
    }

    public List<Info> getAll() { return infoRepository.findAll(); }
    public Optional<Info> getById(Long id) { return infoRepository.findById(id); }
    public Info create(Info info) { return infoRepository.save(info); }
    public Info update(Long id, Info info) {
        return infoRepository.findById(id).map(existing -> {
            existing.setName(info.getName());
            existing.setCount(info.getCount());
            return infoRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Info not found"));
    }
    public void delete(Long id) { infoRepository.deleteById(id); }
}
