package com.example.lims.service;

import com.example.lims.entity.Location;
import com.example.lims.entity.Publisher;
import com.example.lims.entity.SystemParameter;
import com.example.lims.entity.status.Genre;
import com.example.lims.entity.status.Language;
import com.example.lims.exception.BusinessLogicException;
import com.example.lims.exception.ResourceNotFoundException;
import com.example.lims.repository.GenreRepository;
import com.example.lims.repository.LanguageRepository;
import com.example.lims.repository.LocationRepository;
import com.example.lims.repository.PublisherRepository;
import com.example.lims.repository.SystemParameterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Административное управление справочниками-классификаторами и системными параметрами.
 */
@Service
@Transactional
public class AdminDictionaryService {

    private static final Set<String> EDITABLE_TYPES =
            Set.of("genres", "languages", "publishers", "locations");

    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final PublisherRepository publisherRepository;
    private final LocationRepository locationRepository;
    private final SystemParameterRepository systemParameterRepository;
    private final AuditService auditService;
    private final EntityManager entityManager;

    public AdminDictionaryService(GenreRepository genreRepository,
                                  LanguageRepository languageRepository,
                                  PublisherRepository publisherRepository,
                                  LocationRepository locationRepository,
                                  SystemParameterRepository systemParameterRepository,
                                  AuditService auditService,
                                  EntityManager entityManager) {
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.publisherRepository = publisherRepository;
        this.locationRepository = locationRepository;
        this.systemParameterRepository = systemParameterRepository;
        this.auditService = auditService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(String type) {
        String editable = requireEditable(type);

        return switch (editable) {
            case "genres" -> genreRepository.findAll().stream()
                    .sorted(Comparator.comparing(Genre::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(genre -> row(genre.getId(), "name", genre.getName()))
                    .toList();
            case "languages" -> languageRepository.findAll().stream()
                    .sorted(Comparator.comparing(Language::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(language -> row(language.getId(), "name", language.getName()))
                    .toList();
            case "publishers" -> publisherRepository.findAll().stream()
                    .sorted(Comparator.comparing(Publisher::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(publisher -> {
                        Map<String, Object> row = row(publisher.getId(), "name", publisher.getName());
                        row.put("city", publisher.getCity());
                        return row;
                    })
                    .toList();
            case "locations" -> locationRepository.findAll().stream()
                    .sorted(Comparator.comparing(location -> locationName(location), String.CASE_INSENSITIVE_ORDER))
                    .map(location -> {
                        Map<String, Object> row = row(location.getId(), "hall", location.getHall());
                        row.put("shelf", location.getShelf());
                        row.put("rack", location.getRack());
                        row.put("name", locationName(location));
                        return row;
                    })
                    .toList();
            default -> List.of();
        };
    }

    public Map<String, Object> create(String type, Map<String, String> body) {
        String editable = requireEditable(type);

        return switch (editable) {
            case "genres" -> {
                Genre genre = new Genre();
                genre.setName(requireText(body, "name"));
                genreRepository.save(genre);
                auditService.log("CREATE", "DICTIONARY_GENRES", genre.getId());
                yield row(genre.getId(), "name", genre.getName());
            }
            case "languages" -> {
                Language language = new Language();
                language.setName(requireText(body, "name"));
                languageRepository.save(language);
                auditService.log("CREATE", "DICTIONARY_LANGUAGES", language.getId());
                yield row(language.getId(), "name", language.getName());
            }
            case "publishers" -> {
                Publisher publisher = new Publisher();
                publisher.setName(requireText(body, "name"));
                publisher.setCity(trimToNull(body.get("city")));
                publisherRepository.save(publisher);
                auditService.log("CREATE", "DICTIONARY_PUBLISHERS", publisher.getId());
                Map<String, Object> row = row(publisher.getId(), "name", publisher.getName());
                row.put("city", publisher.getCity());
                yield row;
            }
            case "locations" -> {
                Location location = new Location();
                applyLocation(location, body);
                locationRepository.save(location);
                auditService.log("CREATE", "DICTIONARY_LOCATIONS", location.getId());
                yield locationRow(location);
            }
            default -> throw new BusinessLogicException("Неизвестный тип справочника: " + type);
        };
    }

    public Map<String, Object> update(String type, UUID id, Map<String, String> body) {
        String editable = requireEditable(type);

        return switch (editable) {
            case "genres" -> {
                Genre genre = genreRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден: " + id));
                genre.setName(requireText(body, "name"));
                genreRepository.save(genre);
                auditService.log("UPDATE", "DICTIONARY_GENRES", genre.getId());
                yield row(genre.getId(), "name", genre.getName());
            }
            case "languages" -> {
                Language language = languageRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Язык не найден: " + id));
                language.setName(requireText(body, "name"));
                languageRepository.save(language);
                auditService.log("UPDATE", "DICTIONARY_LANGUAGES", language.getId());
                yield row(language.getId(), "name", language.getName());
            }
            case "publishers" -> {
                Publisher publisher = publisherRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Издательство не найдено: " + id));
                publisher.setName(requireText(body, "name"));
                publisher.setCity(trimToNull(body.get("city")));
                publisherRepository.save(publisher);
                auditService.log("UPDATE", "DICTIONARY_PUBLISHERS", publisher.getId());
                Map<String, Object> row = row(publisher.getId(), "name", publisher.getName());
                row.put("city", publisher.getCity());
                yield row;
            }
            case "locations" -> {
                Location location = locationRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Место хранения не найдено: " + id));
                applyLocation(location, body);
                locationRepository.save(location);
                auditService.log("UPDATE", "DICTIONARY_LOCATIONS", location.getId());
                yield locationRow(location);
            }
            default -> throw new BusinessLogicException("Неизвестный тип справочника: " + type);
        };
    }

    public void delete(String type, UUID id) {
        String editable = requireEditable(type);

        switch (editable) {
            case "genres" -> {
                Genre genre = genreRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Жанр не найден: " + id));
                deleteSafely(() -> genreRepository.delete(genre));
                auditService.log("DELETE", "DICTIONARY_GENRES", id);
            }
            case "languages" -> {
                Language language = languageRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Язык не найден: " + id));
                deleteSafely(() -> languageRepository.delete(language));
                auditService.log("DELETE", "DICTIONARY_LANGUAGES", id);
            }
            case "publishers" -> {
                Publisher publisher = publisherRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Издательство не найдено: " + id));
                deleteSafely(() -> publisherRepository.delete(publisher));
                auditService.log("DELETE", "DICTIONARY_PUBLISHERS", id);
            }
            case "locations" -> {
                Location location = locationRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Место хранения не найдено: " + id));
                deleteSafely(() -> locationRepository.delete(location));
                auditService.log("DELETE", "DICTIONARY_LOCATIONS", id);
            }
            default -> throw new BusinessLogicException("Неизвестный тип справочника: " + type);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listParameters() {
        return systemParameterRepository.findAll().stream()
                .sorted(Comparator.comparing(SystemParameter::getKey))
                .map(parameter -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("key", parameter.getKey());
                    row.put("value", parameter.getValue());
                    row.put("description", parameter.getDescription() != null ? parameter.getDescription() : "");
                    return row;
                })
                .toList();
    }

    public Map<String, Object> updateParameter(String key, String value) {
        SystemParameter parameter = systemParameterRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("Параметр не найден: " + key));
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessLogicException("Значение параметра обязательно");
        }

        if ("fine_per_day".equals(key) || "loan_period_days".equals(key)
                || "max_active_loans_default".equals(key)) {
            try {
                BigDecimal numeric = new BigDecimal(normalized.replace(',', '.'));
                if (numeric.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException("non-positive");
                }
            } catch (NumberFormatException ex) {
                throw new BusinessLogicException("Параметр должен быть положительным числом");
            }
        }

        parameter.setValue(normalized);
        systemParameterRepository.save(parameter);
        auditService.log("UPDATE", "SYSTEM_PARAMETER", UUID.nameUUIDFromBytes(key.getBytes()));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", parameter.getKey());
        row.put("value", parameter.getValue());
        row.put("description", parameter.getDescription() != null ? parameter.getDescription() : "");
        return row;
    }

    private String requireEditable(String type) {
        String normalized = type != null ? type.trim().toLowerCase() : "";
        if (!EDITABLE_TYPES.contains(normalized)) {
            throw new BusinessLogicException("Справочник недоступен для редактирования: " + type);
        }
        return normalized;
    }

    private void deleteSafely(Runnable action) {
        try {
            action.run();
            entityManager.flush();
        } catch (DataIntegrityViolationException | PersistenceException ex) {
            throw new BusinessLogicException("Нельзя удалить запись: она используется в других данных");
        }
    }

    private void applyLocation(Location location, Map<String, String> body) {
        String hall = trimToNull(body.get("hall"));
        String shelf = trimToNull(body.get("shelf"));
        String rack = trimToNull(body.get("rack"));
        if (hall == null && shelf == null && rack == null) {
            throw new BusinessLogicException("Укажите хотя бы одно поле места хранения");
        }
        location.setHall(hall);
        location.setShelf(shelf);
        location.setRack(rack);
    }

    private Map<String, Object> locationRow(Location location) {
        Map<String, Object> row = row(location.getId(), "hall", location.getHall());
        row.put("shelf", location.getShelf());
        row.put("rack", location.getRack());
        row.put("name", locationName(location));
        return row;
    }

    private Map<String, Object> row(UUID id, String field, String value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put(field, value);
        return row;
    }

    private String locationName(Location location) {
        List<String> parts = new ArrayList<>();
        if (location.getHall() != null && !location.getHall().isBlank()) parts.add(location.getHall());
        if (location.getShelf() != null && !location.getShelf().isBlank()) parts.add(location.getShelf());
        if (location.getRack() != null && !location.getRack().isBlank()) parts.add(location.getRack());
        return parts.isEmpty() ? "Без места хранения" : String.join(", ", parts);
    }

    private String requireText(Map<String, String> body, String field) {
        String value = body != null ? trimToNull(body.get(field)) : null;
        if (value == null) {
            throw new BusinessLogicException("Поле «" + field + "» обязательно");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
