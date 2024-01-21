package com.conversion.currencyConverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverter {

	private final static String baseUrl = "https://economia.awesomeapi.com.br/json/last/";

	/**
	 * @param args
	 * a @String containing the amount followed by a space
	 * and the currency
	 */
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter amount and currency e.g. \"100 pound\"");
		String[] convParams = sc.nextLine().split(" ");
		if(convParams.length != 2) {
			System.out.print("Invalid conversion parameters!");
		} else {
			CurrencyConverter cc = new CurrencyConverter();
			cc.run(convParams[0], convParams[1]);
		}
	}

	public Map<String, Double> callApi(String currencyUrl) throws Exception {
		URL url = new URL(currencyUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder content = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		int start = content.indexOf(".");
		String conversionString = content.substring(start - 1, start + 4);
		if(conversionString.contains("\"")) {
			conversionString = conversionString.replace("\"", "");
		}
		Map<String, Double> ratesMap = new HashMap<>();
		ratesMap.put(currencyUrl.substring(currencyUrl.length() - 7), Double.valueOf(conversionString));
		in.close();
		con.disconnect();

		return ratesMap;
	}

	public void run(String amount, String currency) {
		long start = System.currentTimeMillis();

		double dollar = 0;
		double euro = 0;
		double pound = 0;
		double result = 0;
		try {
			result = Double.parseDouble(amount);
		}catch(NumberFormatException nfe) {
			System.out.print("\"" + amount + "\"" + " is not a valid number!");
			System.exit(0);
		}
		Map<String, Double> rates = new HashMap<>();
		try {
			rates = callApi(baseUrl + "USD-EUR");
			rates.putAll(callApi(baseUrl + "USD-GBP"));
			rates.putAll(callApi(baseUrl + "EUR-USD"));
			rates.putAll(callApi(baseUrl + "EUR-GBP"));
			rates.putAll(callApi(baseUrl + "GBP-USD"));
			rates.putAll(callApi(baseUrl + "GBP-EUR"));
		}catch(Exception ex) {
			System.out.println("Error fetching exchange rates!");
			ex.printStackTrace();
		}
		if(currency.equalsIgnoreCase(Currency.Dollar.getName())) {
			dollar = result;
			euro = round((result * rates.get("USD-EUR")), 2);
			pound = round((result * rates.get("USD-GBP")), 2);
		} else if(currency.equalsIgnoreCase(Currency.Euro.getName())) {
			euro = result;
			dollar = round((result * rates.get("EUR-USD")), 2);
			pound = round((result * rates.get("EUR-GBP")), 2);
		} else if(currency.equalsIgnoreCase(Currency.Pound.getName())) {
			pound = result;
			dollar = round((result * rates.get("GBP-USD")), 2);
			euro = round((result * rates.get("GBP-EUR")), 2);
		} else {
			System.out.println("Sorry this currency cannot be converted");
		}
		System.out.print(java.util.Currency.getInstance("GBP").getSymbol() + pound + "\t\t");
		System.out.print("$" + dollar + "\t\t");
		System.out.println(java.util.Currency.getInstance("EUR").getSymbol() + euro + "\t\t");

		long end = System.currentTimeMillis();
		System.out.println("Process completed in " + (end - start) / 1000 + " seconds");
	}

	public static double round(double value, int places) {
	    if (places < 0) {
			throw new IllegalArgumentException();
		}

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}
