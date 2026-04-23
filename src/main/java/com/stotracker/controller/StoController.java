package com.stotracker.controller;

import com.stotracker.model.StoData;
import com.stotracker.service.Result;
import com.stotracker.service.StoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class StoController {

    private final StoDataService service;

    public StoController(StoDataService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(Model model) {
        Result<List<StoData>> result = service.getAllData();
        model.addAttribute("stoDataList", result.data());
        return "index";
    }

    @PostMapping("/add")
    public String addName(@RequestParam String name) {
        service.addName(name);
        return "redirect:/";
    }

    @PostMapping("/update")
    public String updateData(
            @RequestParam Long id,
            @RequestParam(required = false) Integer dilithium,
            @RequestParam(required = false) Integer credits) {
        service.updateData(id, dilithium, credits);
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteName(@RequestParam String name) {
        service.deleteByName(name);
        return "redirect:/";
    }

    @PostMapping("/timestamp")
    public String recordTimestamp(
            @RequestParam Long id,
            @RequestParam String type) {
        service.recordTimestamp(id, type);
        return "redirect:/";
    }

    @PostMapping("/untimestamp")
    public String clearTimestamp(
            @RequestParam Long id,
            @RequestParam String type) {
        service.clearTimestamp(id, type);
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleError(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}