package pl.frodo.barber.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.frodo.barber.model.ServiceItem;
import pl.frodo.barber.repository.ServiceRepository;

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
