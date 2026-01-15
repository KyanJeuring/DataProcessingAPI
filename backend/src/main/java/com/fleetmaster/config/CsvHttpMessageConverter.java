package com.fleetmaster.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * HTTP Message Converter for CSV format
 * Implements RFC 4180 compliant CSV serialization
 */
public class CsvHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CsvHttpMessageConverter() {
        super(MediaType.parseMediaType("text/csv"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true; // Support all classes, convert to CSV if possible
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        throw new UnsupportedOperationException("CSV reading is not supported");
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        
        outputMessage.getHeaders().setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        
        try (Writer writer = new OutputStreamWriter(outputMessage.getBody(), StandardCharsets.UTF_8)) {
            // Convert object to List<Map> structure
            List<Map<String, Object>> rows = convertToMapList(object);
            
            if (rows == null || rows.isEmpty()) {
                writer.write("");
                return;
            }
            
            // Extract headers from first row (maintain order)
            Set<String> headers = new LinkedHashSet<>(rows.get(0).keySet());
            
            // Write header row
            writer.write(String.join(",", headers.stream()
                    .map(this::escapeCsvValue)
                    .toArray(String[]::new)));
            writer.write("\r\n");
            
            // Write data rows
            for (Map<String, Object> row : rows) {
                String[] values = headers.stream()
                        .map(header -> {
                            Object value = row.get(header);
                            return value != null ? value.toString() : "";
                        })
                        .map(this::escapeCsvValue)
                        .toArray(String[]::new);
                
                writer.write(String.join(",", values));
                writer.write("\r\n");
            }
            
            writer.flush();
        }
    }

    /**
     * Convert object to List<Map<String, Object>> structure
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertToMapList(Object object) {
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                return List.of();
            }
            
            // Check if it's already a List<Map>
            if (list.get(0) instanceof Map) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> mapList = (List<Map<String, Object>>) (List<?>) object;
                return mapList;
            }
            
            // Convert list of objects to list of maps using Jackson
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> converted = (List<Map<String, Object>>) (List<?>) list.stream()
                    .map(item -> (Map<String, Object>) objectMapper.convertValue(item, Map.class))
                    .toList();
            return converted;
        } else if (object instanceof Map) {
            // Single map, wrap in list
            return List.of((Map<String, Object>) object);
        } else {
            // Convert single object to map and wrap in list
            Map<String, Object> map = objectMapper.convertValue(object, Map.class);
            return List.of(map);
        }
    }

    /**
     * Escape CSV value according to RFC 4180
     * - Wrap in quotes if contains comma, quote, or newline
     * - Escape quotes by doubling them
     */
    private String escapeCsvValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // Check if value needs quoting
        boolean needsQuoting = value.contains(",") || 
                              value.contains("\"") || 
                              value.contains("\n") || 
                              value.contains("\r");
        
        if (needsQuoting) {
            // Escape quotes by doubling them
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return value;
    }
}
