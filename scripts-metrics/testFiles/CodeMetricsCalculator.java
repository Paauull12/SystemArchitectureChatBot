// Example 5: Metrics Calculator with Combined Metrics
// This class analyzes the previous examples and calculates combined metrics

import java.util.*;

public class CodeMetricsCalculator {

    public static void main(String[] args) {
        System.out.println("=== CODE METRICS ANALYSIS ===\n");

        // Analyze each problematic example
        analyzeExample("ComplexProcessor", 15, 18, 25, 0.3, 3, 8, 0.65);
        analyzeExample("MixedUtilityClass", 6, 8, 28, 0.85, 2, 4, 0.67);
        analyzeExample("TightlyCoupledService", 8, 10, 22, 0.4, 8, 15, 0.65);
        analyzeExample("Car", 12, 14, 35, 0.6, 5, 12, 0.71);

        System.out.println("\n=== THRESHOLD ANALYSIS ===");
        printThresholds();
    }

    public static void analyzeExample(String className, int cyclomatic, int cognitive,
            int wmc, double lcom, int ca, int ce, double instability) {

        System.out.println("--- " + className + " ---");
        System.out.println("Cyclomatic Complexity: " + cyclomatic);
        System.out.println("Cognitive Complexity: " + cognitive);
        System.out.println("WMC (Weighted Methods per Class): " + wmc);
        System.out.println("LCOM (Lack of Cohesion): " + String.format("%.2f", lcom));
        System.out.println("Afferent Coupling (Ca): " + ca);
        System.out.println("Efferent Coupling (Ce): " + ce);
        System.out.println("Instability: " + String.format("%.2f", instability));

        // Combined Metrics as requested (combining metrics 2 & 3)
        double combinedComplexity = (cognitive * wmc) / 10.0; // Cognitive * WMC
        double complexityDensity = (cyclomatic + cognitive) / 2.0;
        double couplingInstability = (ca + ce) * instability;
        double maintainabilityIndex = 100 - ((cyclomatic + cognitive) * 2.5) - (lcom * 50);

        System.out.println("\n--- COMBINED METRICS ---");
        System.out.println("Combined Complexity (Cognitive*WMC/10): " + String.format("%.2f", combinedComplexity));
        System.out.println("Complexity Density: " + String.format("%.2f", complexityDensity));
        System.out.println("Coupling-Instability Factor: " + String.format("%.2f", couplingInstability));
        System.out.println("Maintainability Index: " + String.format("%.2f", maintainabilityIndex));

        // Quality assessment
        assessQuality(cyclomatic, cognitive, wmc, lcom, combinedComplexity, maintainabilityIndex);
        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void assessQuality(int cyclomatic, int cognitive, int wmc, double lcom,
            double combinedComplexity, double maintainabilityIndex) {
        System.out.println("\n--- QUALITY ASSESSMENT ---");

        List<String> issues = new ArrayList<>();

        if (cyclomatic > 10)
            issues.add("HIGH Cyclomatic Complexity");
        if (cognitive > 15)
            issues.add("HIGH Cognitive Complexity");
        if (wmc > 20)
            issues.add("HIGH WMC (too many/complex methods)");
        if (lcom > 0.7)
            issues.add("POOR Cohesion (LCOM)");
        if (combinedComplexity > 30)
            issues.add("CRITICAL Combined Complexity");
        if (maintainabilityIndex < 20)
            issues.add("LOW Maintainability");

        if (issues.isEmpty()) {
            System.out.println("✓ Code quality is ACCEPTABLE");
        } else {
            System.out.println("⚠ ISSUES FOUND:");
            for (String issue : issues) {
                System.out.println("  - " + issue);
            }
        }
    }

    private static void printThresholds() {
        System.out.println("Recommended Thresholds:");
        System.out.println("• Cyclomatic Complexity: ≤ 10");
        System.out.println("• Cognitive Complexity: ≤ 15");
        System.out.println("• WMC: ≤ 20");
        System.out.println("• LCOM: ≤ 0.5");
        System.out.println("• Instability: 0.2-0.8 (balanced)");
        System.out.println("• Combined Complexity: ≤ 25");
        System.out.println("• Maintainability Index: ≥ 20");

        System.out.println("\nProblem Indicators:");
        System.out.println("🔴 High Complexity: Difficult to test and maintain");
        System.out.println("🔴 Poor Cohesion: Class does too many unrelated things");
        System.out.println("🔴 High Coupling: Changes ripple through many classes");
        System.out.println("🔴 High Instability: Class changes frequently");
    }

    // Utility method to simulate real metric calculation
    public static Map<String, Double> calculateRealMetrics(String sourceCode) {
        Map<String, Double> metrics = new HashMap<>();

        // Simple heuristic calculations based on code patterns
        int ifCount = countOccurrences(sourceCode, "if");
        int forCount = countOccurrences(sourceCode, "for");
        int whileCount = countOccurrences(sourceCode, "while");
        int switchCount = countOccurrences(sourceCode, "switch");
        int catchCount = countOccurrences(sourceCode, "catch");

        // Cyclomatic Complexity approximation
        double cyclomatic = 1 + ifCount + forCount + whileCount + switchCount + catchCount;

        // Method count approximation
        int methodCount = countOccurrences(sourceCode, "public ") +
                countOccurrences(sourceCode, "private ") +
                countOccurrences(sourceCode, "protected ");

        metrics.put("cyclomatic", cyclomatic);
        metrics.put("methods", (double) methodCount);
        metrics.put("wmc", cyclomatic * methodCount / 3.0);

        return metrics;
    }

    private static int countOccurrences(String text, String pattern) {
        return text.split(pattern, -1).length - 1;
    }
}