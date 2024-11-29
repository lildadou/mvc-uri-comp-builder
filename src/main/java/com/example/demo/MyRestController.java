package com.example.demo;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping(path = "/foo")
public class MyRestController {
    @GetMapping(path = "/bar")
    public ResponseEntity<List<Integer>> search(@ModelAttribute @NotNull BarRequest barRequest, @RequestParam(defaultValue = "10") Integer limit) {
        final BarRequest disambiguatedBarRequest = barRequest.disambiguated();
        if (barRequest != disambiguatedBarRequest) {
            return ResponseEntity
                    .status(HttpStatus.PERMANENT_REDIRECT)
                    .header(HttpHeaders.LOCATION, MvcUriComponentsBuilder
                            .fromMethodName(this.getClass(), "search", disambiguatedBarRequest, limit).build()
                            .encode().toUri().toASCIIString())
                    .build();
        }

        return ResponseEntity.ok(generateRandomArray(limit, 0, 100));
    }

    // credits: Ashish Lahoti
    public static List<Integer> generateRandomArray(int size, int min, int max) {
        return IntStream
                .generate(() -> min + new Random().nextInt(max - min + 1))
                .limit(size).boxed().toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BarRequest {
        private Set<String> categories;
        private Set<String> otherFilters;

        public BarRequest disambiguated() {
            BarRequest candidate = BarRequest.builder()
                    .categories(disambiguateStringSet(categories))
                    .otherFilters(disambiguateStringSet(otherFilters))
                    .build();
            if (Objects.equals(candidate, this)) {
                return this;
            } else {
                return candidate;
            }
        }

        private Set<String> disambiguateStringSet(Set<String> set) {
            return Optional.ofNullable(set)
                    .map(categories -> categories.stream().filter(Objects::nonNull).collect(Collectors.toSet()))
                    .filter(c -> !c.isEmpty())
                    .orElse(null);
        }
    }
}
