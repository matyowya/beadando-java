package org.example.beadandojava;


import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ForexController {

    @GetMapping("/account_summary")
    public String account_info(Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();
            model.addAttribute("summary", summary);
        } catch (Exception e) {
            model.addAttribute("error", "Nem sikerült lekérni a számlainformációkat.");
            e.printStackTrace();
        }
        return "";
    }

    //forex-aktár
    @GetMapping("/actual_prices")
    public String actual_prices(Model model) {
        model.addAttribute("par", new MessageActPrice()); // A tananyagban "par" a neve
        return "";
    }

    @PostMapping("/actual_prices")
    public String actual_prices2(Model model) {

        return "";
    }

    //forex-histár
    @GetMapping("/hist_prices")
    public String hist_prices(Model model) {
        model.addAttribute("par", new MessageHistPrice());
        return "";
    }

    @PostMapping("/hist_prices")
    public String hist_prices2(Model model) {
        return "";
    }

    //forex-nyit
    @GetMapping("/open_position")
    public String open_position(Model model) {
        model.addAttribute("param", new MessageOpenPosition());
        return "";
    }

    @PostMapping("/open_position")
    public String open_position2(Model model) {
        return "";
    }

    //forex-poz
    @GetMapping("/positions")
    @ResponseBody
    public String positions() {

        return null;
    }

    //forex-zar
    @GetMapping("/close_position")
    public String close_position(Model model) {
        model.addAttribute("param", new MessageClosePosition());
        return "form_close_position";
    }

    @PostMapping("/close_position")
    public String close_position2(Model model) {

        return "result_close_position";
    }


    public static class MessageActPrice {
        private String instrument;
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
    }

    public static class MessageHistPrice {
        private String instrument;
        private String granularity;
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
        public String getGranularity() { return granularity; }
        public void setGranularity(String granularity) { this.granularity = granularity; }
    }

    public static class MessageOpenPosition {
        private String instrument;
        private int units;
        public String getInstrument() { return instrument; }
        public void setInstrument(String instrument) { this.instrument = instrument; }
        public int getUnits() { return units; }
        public void setUnits(int units) { this.units = units; }
    }

    public static class MessageClosePosition {
        private int tradeId;
        public int getTradeId() { return tradeId; }
        public void setTradeId(int tradeId) { this.tradeId = tradeId; }
    }
}