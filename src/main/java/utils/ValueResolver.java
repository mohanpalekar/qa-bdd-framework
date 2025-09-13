package utils;

import com.github.javafaker.Faker;
import context.ScenarioContext;
import com.qa.bdd.steps.Hooks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public final class ValueResolver {
    private static final Faker FAKER = new Faker();
    private static final SecureRandom RANDOM = new SecureRandom();

    private ValueResolver() {
    } // utility class

    public static String resolve(String rawValue) {
        Logger logger = Hooks.getLogger();

        if (rawValue == null || rawValue.isBlank()) return "";

        if (logger != null) logger.info(() -> "🔍 Resolving value token: " + rawValue);

        if (rawValue.startsWith("${context:") && rawValue.endsWith("}")) {
            return resolveFromContext(rawValue, logger);
        }

        if (rawValue.startsWith("${vault:") && rawValue.endsWith("}")) {
            return resolveFromVault(rawValue, logger);
        }

        if (rawValue.startsWith("${") && rawValue.endsWith("}") && rawValue.contains("=")) {
            return resolveAndStore(rawValue, logger);
        }

        if (rawValue.startsWith("${") && rawValue.endsWith("}")) {
            String tokenPattern = rawValue.substring(2, rawValue.length() - 1);
            String val = generateCombination(tokenPattern);
            if (logger != null) logger.info(() ->String.format("✨ Generated %s -> %s", tokenPattern, val));
            return val;
        }

        return rawValue;
    }

    private static String resolveFromContext(String rawValue, Logger logger) {
        String key = rawValue.substring(10, rawValue.length() - 1);
        String val = ScenarioContext.getString(key);
        if (logger != null) logger.info(() -> "📦 Using context value for key: " + key + " -> " + val);
        return val;
    }

    private static String resolveFromVault(String rawValue, Logger logger) {
        String secretKey = rawValue.substring(8, rawValue.length() - 1);
        String val = "fetched-secret-" + secretKey; // placeholder
        if (logger != null) logger.info(() -> "🔑 Using vault secret for key: " + secretKey);
        return val;
    }

    private static String resolveAndStore(String rawValue, Logger logger) {
        String inside = rawValue.substring(2, rawValue.length() - 1);
        String[] parts = inside.split("=", 2);
        String key = parts[0].trim();
        String tokenPattern = parts[1].trim();
        String value = generateCombination(tokenPattern);
        ScenarioContext.put(key, value);
        if (logger != null)
            logger.info(()->String.format("✨ Generated and saved %s for key %s -> %s", tokenPattern, key, value));
        return value;
    }

    private static String generateCombination(String tokenPattern) {
        String[] tokens = tokenPattern.split("\\+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) sb.append(generateSingle(token.trim()));
        return sb.toString();
    }

    private static String generateSingle(String token) {
        token = token.toLowerCase();

        if (token.startsWith("date:")) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern(token.split(":", 2)[1]));
        }
        if (token.startsWith("futuredate:")) {
            String[] parts = token.split(":");
            int days = Integer.parseInt(parts[1]);
            String fmt = parts.length > 2 ? parts[2] : "yyyy-MM-dd";
            return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern(fmt));
        }
        if (token.startsWith("number:")) return randomNumeric(Integer.parseInt(token.split(":")[1]));
        if (token.startsWith("string:")) return randomString(Integer.parseInt(token.split(":")[1]));
        if (token.startsWith("float:")) {
            String[] parts = token.split(":");
            double max = Math.pow(10, Integer.parseInt(parts[1])) - 1;
            int decimalPlaces = 2; // default
            if (parts.length > 2) {
                try {
                    decimalPlaces = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    Hooks.getLogger().info(() -> "NumberFormatException");
                }
            }
            double val = new SecureRandom().nextDouble() * (max - 1) + 1;
            BigDecimal bd = BigDecimal.valueOf(val).setScale(decimalPlaces, RoundingMode.HALF_UP);
            return bd.toPlainString();

        }

        return switch (token) {
            case "firstname" -> FAKER.name().firstName();
            case "lastname" -> FAKER.name().lastName();
            case "email" -> FAKER.internet().emailAddress();
            case "phonenumber" -> FAKER.phoneNumber().cellPhone();
            case "address" -> FAKER.address().fullAddress();
            default -> throw new UnknownTokenException(token);
        };
    }

    private static String randomNumeric(int digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    private static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }

    public static class UnknownTokenException extends RuntimeException {
        public UnknownTokenException(String token) {
            super("Unknown token: " + token);
        }
    }
}
