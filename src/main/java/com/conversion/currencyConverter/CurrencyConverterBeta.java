package com.conversion.currencyConverter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverterBeta {
    public static String url = "https://economia.awesomeapi.com.br/last/USD-EUR,USD-GBP,EUR-GBP,USD-ISK,EUR-ISK";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter amount and currency e.g. \"100 pound\"");
        String[] convParams = sc.nextLine().split(" ");
        sc.close();
        if (convParams.length != 2) {
            System.out.print("Invalid conversion parameters!");
        } else {
            CurrencyConverterBeta ccBeta = new CurrencyConverterBeta();
            ccBeta.run(convParams[0], convParams[1]);
        }
    }

    public void run(String amount, String currency) {
        long start = System.currentTimeMillis();

        double dollar = 0;
        double euro = 0;
        double pound = 0;
        double krona = 0;
        double result = 0;
        try {
            result = Double.parseDouble(amount);
        } catch (NumberFormatException nfe) {
            System.out.print("\"" + amount + "\"" + " is not a valid number!");
            System.exit(0);
        }
        Map<String, Double> rates = fillRatesMap();

        if (currency.equalsIgnoreCase(Currency.Krona.getName())) {
            krona = result;
            dollar = round((result * rates.get("ISKUSD")), 2);
            euro = round((result * rates.get("ISKEUR")), 2);
            pound = round((result * (rates.get("ISKUSD") * rates.get("USDGBP"))), 2);
        } else if (currency.equalsIgnoreCase(Currency.Dollar.getName())) {
            dollar = result;
            euro = round((result * rates.get("USDEUR")), 2);
            pound = round((result * rates.get("USDGBP")), 2);
            krona = round((result * rates.get("USDISK")), 2);
        } else if (currency.equalsIgnoreCase(Currency.Euro.getName())) {
            euro = result;
            dollar = round((result * rates.get("EURUSD")), 2);
            pound = round((result * rates.get("EURGBP")), 2);
            krona = round((result * rates.get("EURISK")), 2);
        } else if (currency.equalsIgnoreCase(Currency.Pound.getName())) {
            pound = result;
            dollar = round((result * rates.get("GBPUSD")), 2);
            euro = round((result * rates.get("GBPEUR")), 2);
            krona = round((result * (rates.get("GBPEUR")) * rates.get("EURISK")), 2);
        } else {
            System.out.println("Sorry this currency cannot be converted");
        }
        System.out.print(java.util.Currency.getInstance("GBP").getSymbol() + pound + "\t\t");
        System.out.print("$" + dollar + "\t\t");
        System.out.print(java.util.Currency.getInstance("EUR").getSymbol() + euro + "\t\t");
        System.out.println(java.util.Currency.getInstance("ISK").getSymbol() + krona + "\t\t");

        long end = System.currentTimeMillis();
        System.out.println("Process completed in " + (end - start) / 1000 + " second(s)");
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String callApi(String currencyUrl) throws Exception {
        URL url = new URL(currencyUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    public JSONObject parseResponse() {
        String response = "";
        try {
            response = callApi(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject(response);
    }

    public Map<String, Double> fillRatesMap() {
        Map<String, Double> ratesMap = new HashMap<>();
        JSONObject responseAsJson = parseResponse();
        JSONObject dollarEuro = responseAsJson.getJSONObject("USDEUR");
        JSONObject dollarKrona = responseAsJson.getJSONObject("USDISK");
        JSONObject dollarPound = responseAsJson.getJSONObject("USDGBP");
        JSONObject euroPound = responseAsJson.getJSONObject("EURGBP");
        JSONObject euroKrona = responseAsJson.getJSONObject("EURISK");

        ratesMap.put("USDEUR", Double.valueOf(dollarEuro.get("high").toString()));
        ratesMap.put("USDISK", Double.valueOf(dollarKrona.get("high").toString()));
        ratesMap.put("ISKUSD", 1 / Double.parseDouble(dollarKrona.get("low").toString()));
        ratesMap.put("EURUSD", 1 / Double.parseDouble(dollarEuro.get("low").toString()));
        ratesMap.put("USDGBP", Double.valueOf(dollarPound.get("high").toString()));
        ratesMap.put("GBPUSD", 1 / Double.parseDouble(dollarPound.get("low").toString()));
        ratesMap.put("EURGBP", Double.valueOf(euroPound.get("high").toString()));
        ratesMap.put("EURISK", Double.valueOf(euroKrona.get("high").toString()));
        ratesMap.put("ISKEUR", 1 / Double.parseDouble(euroKrona.get("low").toString()));
        ratesMap.put("GBPEUR", 1 / Double.parseDouble(euroPound.get("low").toString()));

        return ratesMap;
    }
}
