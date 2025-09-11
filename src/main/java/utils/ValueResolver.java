package utils;

import com.github.javafaker.Faker;
import context.ScenarioContext;
import steps.Hooks;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class ValueResolver {
    private static final Faker faker = new Faker();
    private static final SecureRandom random = new SecureRandom();

    public static String resolve(String rawValue) {
        Logger logger = Hooks.getLogger();

        if (rawValue == null || rawValue.isBlank()) return "";

        logger.info("🔍 Resolving value token: " + rawValue);

        // Fetch from context
        if (rawValue.startsWith("${context:") && rawValue.endsWith("}")) {
            String key = rawValue.substring(10, rawValue.length() - 1);
            String val = ScenarioContext.getString(key);
            logger.info("📦 Using context value for key: " + key + " -> " + val);
            return val;
        }

        // Vault placeholder
        if (rawValue.startsWith("${vault:") && rawValue.endsWith("}")) {
            String secretKey = rawValue.substring(8, rawValue.length() - 1);
            String val = "fetched-secret-" + secretKey;
            logger.info("🔑 Using vault secret for key: " + secretKey);
            return val;
        }

        // Store generated value: ${key=token} or combination: ${key=firstname+number:3}
        if (rawValue.startsWith("${") && rawValue.endsWith("}") && rawValue.contains("=")) {
            String inside = rawValue.substring(2, rawValue.length() - 1);
            String[] parts = inside.split("=", 2);
            String key = parts[0].trim();
            String tokenPattern = parts[1].trim();
            String value = generateCombination(tokenPattern);
            ScenarioContext.put(key, value);
            logger.info("✨ Generated and saved " + tokenPattern + " for key " + key + " -> " + value);
            return value;
        }

        // Normal token or combination without context storage
        if (rawValue.startsWith("${") && rawValue.endsWith("}")) {
            String tokenPattern = rawValue.substring(2, rawValue.length() - 1);
            String val = generateCombination(tokenPattern);
            logger.info("✨ Generated " + tokenPattern + " -> " + val);
            return val;
        }

        return rawValue;
    }

    private static String generateCombination(String tokenPattern) {
        String[] tokens = tokenPattern.split("\\+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append(generateSingle(token.trim()));
        }
        return sb.toString();
    }

    private static String generateSingle(String token) {
        token = token.toLowerCase();

        if (token.startsWith("date:")) {
            String fmt = token.split(":", 2)[1];
            return LocalDate.now().format(DateTimeFormatter.ofPattern(fmt));
        }
        if (token.startsWith("futuredate:")) {
            String[] parts = token.split(":");
            int days = Integer.parseInt(parts[1]);
            String fmt = parts.length > 2 ? parts[2] : "yyyy-MM-dd";
            return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(fmt));
        }
        if (token.startsWith("number:")) {
            int digits = Integer.parseInt(token.split(":")[1]);
            return randomNumeric(digits);
        }
        if (token.startsWith("string:")) {
            int len = Integer.parseInt(token.split(":")[1]);
            return randomString(len);
        }
        if (token.startsWith("float:")) {
            String[] parts = token.split(":");
            int before = Integer.parseInt(parts[1]);
            int after = Integer.parseInt(parts[2]);
            double max = Math.pow(10, before) - 1;
            double value = ThreadLocalRandom.current().nextDouble(1, max);
            String formatted = String.format("%." + after + "f", value);
            return formatted;
        }

        return switch (token) {
            case "firstname" -> faker.name().firstName();
            case "lastname" -> faker.name().lastName();
            case "email" -> faker.internet().emailAddress();
            case "phonenumber" -> faker.phoneNumber().cellPhone();
            case "address" -> faker.address().fullAddress();
            default -> throw new IllegalArgumentException("Unknown token: " + token);
        };
    }

    private static String randomNumeric(int digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}
