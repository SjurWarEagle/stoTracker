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

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_VALUE = Integer.MAX_VALUE;

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

        if (trimmedName.length() > MAX_NAME_LENGTH) {
            return Result.err("Name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }

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

        if (dilithium != null && (dilithium < 0 || dilithium > MAX_VALUE)) {
            return Result.err("Dilithium must be between 0 and " + MAX_VALUE);
        }

        if (credits != null && (credits < 0 || credits > MAX_VALUE)) {
            return Result.err("Credits must be between 0 and " + MAX_VALUE);
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

        StoData data = repository.findByName(name.trim()).orElse(null);
        if (data == null) {
            return Result.err("Name not found");
        }

        repository.delete(data);
        return Result.ok(data);
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
            case "refining" -> {
                data.setRefiningTime(now);
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
            case "refining" -> {
                data.setRefiningTime(null);
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