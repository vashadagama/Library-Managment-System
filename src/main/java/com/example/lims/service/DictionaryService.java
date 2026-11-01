package com.example.lims.service;

import com.example.lims.dto.DictionaryItemDto;
import com.example.lims.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DictionaryService {

    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final PublisherRepository publisherRepository;
    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final ItemStatusRepository itemStatusRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final AuditTypeRepository auditTypeRepository;

    public DictionaryService(GenreRepository genreRepository,
                             LanguageRepository languageRepository,
                             PublisherRepository publisherRepository,
                             LocationRepository locationRepository,
                             RoleRepository roleRepository,
                             UserStatusRepository userStatusRepository,
                             ItemStatusRepository itemStatusRepository,
                             LoanStatusRepository loanStatusRepository,
                             NotificationTypeRepository notificationTypeRepository,
                             AuditTypeRepository auditTypeRepository) {
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.publisherRepository = publisherRepository;
        this.locationRepository = locationRepository;
        this.roleRepository = roleRepository;
        this.userStatusRepository = userStatusRepository;
        this.itemStatusRepository = itemStatusRepository;
        this.loanStatusRepository = loanStatusRepository;
        this.notificationTypeRepository = notificationTypeRepository;
        this.auditTypeRepository = auditTypeRepository;
    }

    public List<DictionaryItemDto> genres() {
        return genreRepository.findAll().stream()
                .map(g -> new DictionaryItemDto(g.getId(), g.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> languages() {
        return languageRepository.findAll().stream()
                .map(l -> new DictionaryItemDto(l.getId(), l.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> publishers() {
        return publisherRepository.findAll().stream()
                .map(p -> new DictionaryItemDto(p.getId(), p.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> locations() {
        return locationRepository.findAll().stream()
                .map(l -> new DictionaryItemDto(l.getId(), locationName(l.getHall(), l.getShelf(), l.getRack())))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> roles() {
        return roleRepository.findAll().stream()
                .map(r -> new DictionaryItemDto(r.getId(), r.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> userStatuses() {
        return userStatusRepository.findAll().stream()
                .map(s -> new DictionaryItemDto(s.getId(), s.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> itemStatuses() {
        return itemStatusRepository.findAll().stream()
                .map(s -> new DictionaryItemDto(s.getId(), s.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> loanStatuses() {
        return loanStatusRepository.findAll().stream()
                .map(s -> new DictionaryItemDto(s.getId(), s.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> notificationTypes() {
        return notificationTypeRepository.findAll().stream()
                .map(t -> new DictionaryItemDto(t.getId(), t.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    public List<DictionaryItemDto> auditTypes() {
        return auditTypeRepository.findAll().stream()
                .map(t -> new DictionaryItemDto(t.getId(), t.getName()))
                .sorted(Comparator.comparing(DictionaryItemDto::getName))
                .toList();
    }

    private String locationName(String hall, String shelf, String rack) {
        StringBuilder sb = new StringBuilder();
        if (hall != null && !hall.isBlank()) {
            sb.append(hall);
        }
        if (shelf != null && !shelf.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(shelf);
        }
        if (rack != null && !rack.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(rack);
        }
        return sb.isEmpty() ? "Без расположения" : sb.toString();
    }
}
