package com.example.demo.controller;

import com.example.demo.model.ServiceItem;
import com.example.demo.repository.ServiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ServiceRepository serviceRepository;

    public HomeController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/")
    String home(Model model) {
        List<ServiceItem> allServices = serviceRepository.findAll();

        model.addAttribute("allServices", allServices);
        return "home";
    }
}
