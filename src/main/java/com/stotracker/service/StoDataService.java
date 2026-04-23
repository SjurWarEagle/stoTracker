package com.stotracker.service;

import com.stotracker.model.StoData;
import com.stotracker.repository.StoDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StoDataService {

    private final StoDataRepository repository;

    public StoDataService(StoDataRepository repository) {
        this.repository = repository;
    }

    public Result<List<StoData>> getAllData() {
        return Result.ok(repository.findAllByOrderByNameAsc());
    }

    @Transactional
    public Result<StoData> addName(String name) {
        if (name == null || name.isBlank()) {
            return Result.err("Name cannot be empty");
        }

        String trimmedName = name.trim();

        if (repository.findByName(trimmedName).isPresent()) {
            return Result.err("Name already exists");
        }

        StoData newData = new StoData(trimmedName);
        return Result.ok(repository.save(newData));
    }

    @Transactional
    public Result<StoData> updateData(Long id, Integer dilithium, Integer credits) {
        Optional<StoData> optData = repository.findById(id);
        if (optData.isEmpty()) {
            return Result.err("Record not found");
        }

        StoData data = optData.get();

        if (dilithium != null && dilithium >= 0) {
            data.setDilithium(dilithium);
        }

        if (credits != null && credits >= 0) {
            data.setCredits(credits);
        }

        return Result.ok(repository.save(data));
    }

    @Transactional
    public Result<StoData> deleteByName(String name) {
        if (name == null || name.isBlank()) {
            return Result.err("Name cannot be empty");
        }

        Optional<StoData> optData = repository.findByName(name.trim());
        if (optData.isEmpty()) {
            return Result.err("Name not found");
        }

        repository.deleteByName(name.trim());
        return Result.ok(optData.get());
    }

    @Transactional
    public Result<StoData> recordTimestamp(Long id, String timestampType) {
        Optional<StoData> optData = repository.findById(id);
        if (optData.isEmpty()) {
            return Result.err("Record not found");
        }

        StoData data = optData.get();
        LocalDateTime now = LocalDateTime.now();

        return switch (timestampType.toLowerCase()) {
            case "recruitment" -> {
                data.setRecruitmentTime(now);
                yield Result.ok(repository.save(data));
            }
            case "convertion" -> {
                data.setConvertionTime(now);
                yield Result.ok(repository.save(data));
            }
            case "event" -> {
                data.setEventTime(now);
                yield Result.ok(repository.save(data));
            }
            default -> Result.err("Invalid timestamp type");
        };
    }

    @Transactional
    public Result<StoData> clearTimestamp(Long id, String timestampType) {
        Optional<StoData> optData = repository.findById(id);
        if (optData.isEmpty()) {
            return Result.err("Record not found");
        }

        StoData data = optData.get();

        return switch (timestampType.toLowerCase()) {
            case "recruitment" -> {
                data.setRecruitmentTime(null);
                yield Result.ok(repository.save(data));
            }
            case "convertion" -> {
                data.setConvertionTime(null);
                yield Result.ok(repository.save(data));
            }
            case "event" -> {
                data.setEventTime(null);
                yield Result.ok(repository.save(data));
            }
            default -> Result.err("Invalid timestamp type");
        };
    }
}