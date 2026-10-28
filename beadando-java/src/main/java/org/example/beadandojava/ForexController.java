package org.example.beadandojava;


import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeSpecifier;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

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
        return "result_account_summary";
    }

    //forex-aktár
    @GetMapping("/actual_prices")
    public String actual_prices(Model model) {
        model.addAttribute("par", new MessageActPrice());
        return "form_actual_prices";
    }

    @PostMapping("/actual_prices")
    public String actual_prices2(@ModelAttribute MessageActPrice messageActPrice, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String strOut = "";

        List<String> instruments = new ArrayList<>();
        instruments.add(messageActPrice.getInstrument());

        try {
            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instruments);
            PricingGetResponse resp = ctx.pricing.get(request);

            for (ClientPrice price : resp.getPrices()) {
                strOut += "Bid: " + price.getBids().get(0).getPrice() + " | Ask: " + price.getAsks().get(0).getPrice();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("instr", messageActPrice.getInstrument());
        model.addAttribute("price", strOut);
        return "result_actual_prices";
    }

    //forex-histár
    @GetMapping("/hist_prices")
    public String hist_prices(Model model) {
        model.addAttribute("par", new MessageHistPrice());
        return "form_hist_prices";
    }

    @PostMapping("/hist_prices")
    public String hist_prices2(@ModelAttribute MessageHistPrice messageHistPrice, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String strOut = "";

        try {
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(
                    new InstrumentName(messageHistPrice.getInstrument()));

            switch (messageHistPrice.getGranularity()) {
                case "M1":
                    request.setGranularity(CandlestickGranularity.M1);
                    break;
                case "H1":
                    request.setGranularity(CandlestickGranularity.H1);
                    break;
                case "D":
                    request.setGranularity(CandlestickGranularity.D);
                    break;
                case "W":
                    request.setGranularity(CandlestickGranularity.W);
                    break;
                case "M":
                    request.setGranularity(CandlestickGranularity.M);
                    break;
            }
            request.setCount(10L);

            InstrumentCandlesResponse resp = ctx.instrument.candles(request);
            for (Candlestick candle : resp.getCandles()) {
                strOut += "<br>" + candle.getTime().toString().split("T")[0] + ": " +candle.getMid().getC().toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("instr", messageHistPrice.getInstrument());
        model.addAttribute("granularity", messageHistPrice.getGranularity());
        model.addAttribute("price", strOut);
        return "result_hist_prices";
    }

    //forex-nyit
    @GetMapping("/open_position")
    public String open_position(Model model) {
        model.addAttribute("par", new MessageOpenPosition());
        return "form_open_position";
    }

    @PostMapping("/open_position")
    public String open_position2(@ModelAttribute MessageOpenPosition messageOpenPosition, Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        String strOut;
        String price = "";

        try {
            InstrumentName instrument = new InstrumentName(messageOpenPosition.getInstrument());
            OrderCreateRequest request = new OrderCreateRequest(Config.ACCOUNTID);
            MarketOrderRequest marketorderrequest = new MarketOrderRequest();
            marketorderrequest.setInstrument(instrument);
            marketorderrequest.setUnits(messageOpenPosition.getUnits());
            request.setOrder(marketorderrequest);

            OrderCreateResponse response = ctx.order.create(request);
            strOut = "tradeId: " + response.getOrderFillTransaction().getId();
            price = response.getOrderFillTransaction().getPrice().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("instr", messageOpenPosition.getInstrument());
        model.addAttribute("units", messageOpenPosition.getUnits());
        model.addAttribute("price", price);
        model.addAttribute("id", strOut);
        return "result_open_position";
    }

    //forex-poz
    @GetMapping("/positions")
    public String positions(Model model) {
        Context ctx = new Context(Config.URL, Config.TOKEN);
        try {
            List<Trade> trades = ctx.trade.listOpen(Config.ACCOUNTID).getTrades();
            model.addAttribute("trades", trades);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "result_positions";
    }

    //forex-zar
    @GetMapping("/close_position")
    public String close_position(Model model) {
        model.addAttribute("par", new MessageClosePosition());
        return "form_close_position";
    }

    @PostMapping("/close_position")
    public String close_position2(@ModelAttribute MessageClosePosition messageClosePosition, Model model) {
            Context ctx = new Context(Config.URL, Config.TOKEN);
            String tradeId = messageClosePosition.getTradeId() + "";
            String strOut = "Closed tradeId=" + tradeId;

            try {
                ctx.trade.close(new TradeCloseRequest(Config.ACCOUNTID, new TradeSpecifier(tradeId)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            model.addAttribute("tradeId", strOut);
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