package com.example.short;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Short Example 4: High Cognitive Complexity + Low TCC
 * Problems: Cognitive: 50+, TCC: 0.08 (methods don't work together)
 */
public class ProcessingEngine {
    
    // Image processing fields (used only by image methods)
    private String imageDirectory = "/images/";
    private List<String> supportedFormats = Arrays.asList("jpg", "png", "gif");
    private int maxImageSize = 5000000;
    
    // Text processing fields (used only by text methods)
    private Map<String, String> textReplacements = new HashMap<>();
    private Set<String> stopWords = new HashSet<>();
    private String textEncoding = "UTF-8";
    
    // Number processing fields (used only by math methods)
    private double precision = 0.0001;
    private Random numberGenerator = new Random();
    private List<Double> calculationCache = new ArrayList<>();
    
    // File processing fields (used only by file methods)
    private String tempDirectory = "/tmp/";
    private long maxFileSize = 100000000L;
    private Queue<String> processingQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * Complex image processing - Cognitive Complexity: 18
     * Only uses image-related fields
     */
    public ProcessingResult processImageBatch(List<String> imagePaths, Map<String, Object> options) {
        ProcessingResult result = new ProcessingResult();
        List<String> errors = new ArrayList<>();
        int processed = 0;
        
        // Nested validation loops (Cognitive +6)
        for (String imagePath : imagePaths) {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                errors.add("Invalid image path");
                continue;
            }
            
            String extension = imagePath.substring(imagePath.lastIndexOf('.') + 1).toLowerCase();
            if (!supportedFormats.contains(extension)) {
                errors.add("Unsupported format: " + extension);
                continue;
            }
            
            // Complex processing logic with nested conditions (Cognitive +8)
            try {
                if (options.containsKey("resize")) {
                    Map<String, Integer> dimensions = (Map<String, Integer>) options.get("resize");
                    if (dimensions != null) {
                        Integer width = dimensions.get("width");
                        Integer height = dimensions.get("height");
                        
                        if (width != null && height != null) {
                            if (width > 0 && height > 0) {
                                if (width > 4000 || height > 4000) {
                                    errors.add("Dimensions too large for: " + imagePath);
                                    continue;
                                } else {
                                    // Resize logic
                                    for (int attempt = 0; attempt < 3; attempt++) {
                                        try {
                                            // Simulate complex resize operation
                                            if (performImageResize(imagePath, width, height)) {
                                                break;
                                            } else if (attempt == 2) {
                                                errors.add("Resize failed after 3 attempts: " + imagePath);
                                                continue;
                                            }
                                        } catch (Exception e) {
                                            if (attempt == 2) {
                                                errors.add("Resize error: " + e.getMessage());
                                                continue;
                                            }
                                        }
                                        Thread.sleep(100); // Wait between attempts
                                    }
                                }
                            } else {
                                errors.add("Invalid dimensions for: " + imagePath);
                                continue;
                            }
                        }
                    }
                }
                
                // Additional processing options (Cognitive +4)
                if (options.containsKey("filters")) {
                    List<String> filters = (List<String>) options.get("filters");
                    if (filters != null && !filters.isEmpty()) {
                        for (String filter : filters) {
                            if (filter.equals("blur")) {
                                applyBlurFilter(imagePath);
                            } else if (filter.equals("sharpen")) {
                                applySharpenFilter(imagePath);
                            } else if (filter.equals("grayscale")) {
                                applyGrayscaleFilter(imagePath);
                            }
                        }
                    }
                }
                
                processed++;
                
            } catch (Exception e) {
                errors.add("Processing failed for " + imagePath + ": " + e.getMessage());
            }
        }
        
        result.setSuccess(processed > 0);
        result.setProcessedCount(processed);
        result.setErrors(errors);
        return result;
    }
    
    /**
     * Complex text analysis - Cognitive Complexity: 15
     * Only uses text-related fields, no shared variables with image processing
     */
    public AnalysisResult analyzeTextComplexity(String text, boolean enableAdvanced) {
        AnalysisResult result = new AnalysisResult();
        
        if (text == null || text.trim().isEmpty()) {
            result.setValid(false);
            return result;
        }
        
        // Multi-level text processing (Cognitive +8)
        String processedText = text;
        for (Map.Entry<String, String> replacement : textReplacements.entrySet()) {
            processedText = processedText.replace(replacement.getKey(), replacement.getValue());
        }
        
        String[] words = processedText.toLowerCase().split("\\s+");
        List<String> filteredWords = new ArrayList<>();
        
        for (String word : words) {
            if (!stopWords.contains(word)) {
                if (word.length() > 2) {
                    if (enableAdvanced) {
                        if (word.matches("[a-zA-Z]+")) {
                            filteredWords.add(word);
                        }
                    } else {
                        filteredWords.add(word);
                    }
                }
            }
        }
        
        // Complex analysis logic (Cognitive +7)
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : filteredWords) {
            wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
        }
        
        int complexity = 0;
        if (wordFreq.size() > 50) {
            complexity += 3;
            if (wordFreq.size() > 100) {
                complexity += 2;
                if (enableAdvanced) {
                    for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                        if (entry.getValue() > 5) {
                            complexity++;
                        }
                    }
                }
            }
        }
        
        result.setValid(true);
        result.setComplexityScore(complexity);
        return result;
    }
    
    // Number processing - uses only math fields
    public double calculateAdvancedMath(List<Double> numbers, String operation) {
        if (numbers == null || numbers.isEmpty()) return 0.0;
        
        switch (operation) {
            case "variance":
                double mean = numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                return numbers.stream().mapToDouble(n -> Math.pow(n - mean, 2)).average().orElse(0.0);
            case "random":
                return numberGenerator.nextDouble() * 100;
            default:
                return numbers.get(0);
        }
    }
    
    // File processing - uses only file fields
    public void processFileQueue() {
        while (!processingQueue.isEmpty()) {
            String file = processingQueue.poll();
            if (file != null && file.length() < maxFileSize) {
                // Process file
            }
        }
    }
    
    // Helper methods
    private boolean performImageResize(String path, int width, int height) { return true; }
    private void applyBlurFilter(String path) { }
    private void applySharpenFilter(String path) { }
    private void applyGrayscaleFilter(String path) { }
}

class ProcessingResult {
    private boolean success;
    private int processedCount;
    private List<String> errors;
    
    public void setSuccess(boolean success) { this.success = success; }
    public void setProcessedCount(int count) { this.processedCount = count; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}

class AnalysisResult {
    private boolean valid;
    private int complexityScore;
    
    public void setValid(boolean valid) { this.valid = valid; }
    public void setComplexityScore(int score) { this.complexityScore = score; }
}