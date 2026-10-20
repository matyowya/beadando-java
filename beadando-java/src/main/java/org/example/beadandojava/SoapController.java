package org.example.beadandojava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SoapController {

    @Autowired
    private MnbService mnbService;

    @GetMapping("/soap")
    public String soapPage(Model model) {
        return "soap";
    }

    @PostMapping("/soap")
    public String getRates(@RequestParam("startDate") String startDate,
                           @RequestParam("endDate") String endDate,
                           @RequestParam("currency") String currency,
                           Model model) {

        List<MnbService.RateRecord> rates = mnbService.getExchangeRates(startDate, endDate, currency);

        model.addAttribute("rates", rates);
        model.addAttribute("selectedCurrency", currency);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "soap";
    }
}