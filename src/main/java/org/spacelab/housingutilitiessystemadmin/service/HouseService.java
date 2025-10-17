package org.spacelab.housingutilitiessystemadmin.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.entity.location.House;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.mappers.HouseMapper;
import org.spacelab.housingutilitiessystemadmin.models.filters.house.HouseRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseRequest;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponse;
import org.spacelab.housingutilitiessystemadmin.models.location.HouseResponseTable;
import org.spacelab.housingutilitiessystemadmin.repository.HouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class HouseService {

    private final HouseRepository houseRepository;
    private final HouseMapper houseMapper;
    private final StreetService streetService;
    private final ChairmanService chairmanService;

    public Optional<House> findById(String id) {
        return houseRepository.findById(id);
    }

    public House save(House house) {
        return houseRepository.save(house);
    }

    public List<House> findAll() {
        return houseRepository.findAll();
    }

    public void deleteById(String id) {
        houseRepository.deleteById(id);
    }

    public List<House> saveAll(List<House> houses) {
        return houseRepository.saveAll(houses);
    }

    public void deleteAll() {
        houseRepository.deleteAll();
    }

    public HouseResponse getHouseById(String id) {
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new OperationException("получении дома",
                        "Дом с ID " + id + " не найден", HttpStatus.NOT_FOUND));
        log.info("Дом с ID {} успешно получен", id);
        return houseMapper.mapHouseToResponse(house);
    }

    public HouseResponse createHouse(HouseRequest houseRequest) {
        House house = houseMapper.toEntity(houseRequest);
        
        if (houseRequest.getStreetId() != null) {
            Street street = streetService.findById(houseRequest.getStreetId())
                    .orElseThrow(() -> new OperationException("создании дома",
                            "Улица с ID " + houseRequest.getStreetId() + " не найдена", HttpStatus.NOT_FOUND));
            house.setStreet(street);
        }
        
        if (houseRequest.getChairmanId() != null && !houseRequest.getChairmanId().isEmpty()) {
            Chairman chairman = chairmanService.findById(houseRequest.getChairmanId())
                    .orElseThrow(() -> new OperationException("создании дома",
                            "Председатель с ID " + houseRequest.getChairmanId() + " не найден", HttpStatus.NOT_FOUND));
            house.setChairman(chairman);
        }
        
        if (houseRequest.getStatus() != null) {
            try {
                house.setStatus(Status.valueOf(houseRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new OperationException("создании дома",
                        "Неверный статус: " + houseRequest.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
        
        House savedHouse = houseRepository.save(house);
        log.info("Дом с ID {} успешно создан", savedHouse.getId());
        return houseMapper.mapHouseToResponse(savedHouse);
    }

    public HouseResponse updateHouse(String id, HouseRequest houseRequest) {
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new OperationException("обновлении дома",
                        "Дом с ID " + id + " не найден"));

        houseMapper.partialUpdate(houseRequest, house);
        
        if (houseRequest.getStreetId() != null) {
            Street street = streetService.findById(houseRequest.getStreetId())
                    .orElseThrow(() -> new OperationException("обновлении дома",
                            "Улица с ID " + houseRequest.getStreetId() + " не найдена", HttpStatus.NOT_FOUND));
            house.setStreet(street);
        }
        
        if (houseRequest.getChairmanId() != null) {
            if (houseRequest.getChairmanId().isEmpty()) {
                house.setChairman(null);
            } else {
                Chairman chairman = chairmanService.findById(houseRequest.getChairmanId())
                        .orElseThrow(() -> new OperationException("обновлении дома",
                                "Председатель с ID " + houseRequest.getChairmanId() + " не найден", HttpStatus.NOT_FOUND));
                house.setChairman(chairman);
            }
        }
        
        if (houseRequest.getStatus() != null) {
            try {
                house.setStatus(Status.valueOf(houseRequest.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new OperationException("обновлении дома",
                        "Неверный статус: " + houseRequest.getStatus(), HttpStatus.BAD_REQUEST);
            }
        }
        
        House updatedHouse = houseRepository.save(house);
        log.info("Дом с ID {} успешно обновлен", updatedHouse.getId());
        return houseMapper.mapHouseToResponse(updatedHouse);
    }

    public boolean deleteHouse(String id) {
        if (!houseRepository.existsById(id)) {
            throw new OperationException("удалении дома",
                    "Дом с ID " + id + " не найден");
        }
        houseRepository.deleteById(id);
        log.info("Дом с ID {} успешно удален", id);
        return true;
    }

    public Page<HouseResponseTable> getHousesTable(HouseRequestTable houseRequestTable) {
        Page<House> houses = getHouses(houseRequestTable);
        Page<HouseResponseTable> houseResponses = houseMapper.toResponseTablePage(houses);
        log.info("Получена страница домов: страница {}, размер {}", 
                houseRequestTable.getPage(), houseRequestTable.getSize());
        return houseResponses;
    }

    public Page<House> getHouses(HouseRequestTable houseRequestTable) {
        return houseRepository.findHousesWithFilters(houseRequestTable);
    }

    public List<HouseResponse> findByStreetId(String streetId) {
        return houseMapper.toResponseList(houseRepository.findByStreetId(streetId));
    }

    public List<HouseResponse> searchByStreetAndNumber(String streetId, String number) {
        if (number == null || number.trim().isEmpty()) {
            return findByStreetId(streetId);
        }
        return houseMapper.toResponseList(houseRepository.findByStreetIdAndHouseNumberContainingIgnoreCase(streetId, number.trim()));
    }

    @Async
    public CompletableFuture<HouseResponse> findByIdAsync(String id) {
        return CompletableFuture.completedFuture(findById(id)).thenApply(houseOpt -> {
            if (houseOpt.isEmpty()) {
                throw new OperationException("получении дома",
                        "Дом с ID " + id + " не найден", HttpStatus.NOT_FOUND);
            }
            return houseMapper.mapHouseToResponse(houseOpt.get());
        });
    }

    @Async
    public CompletableFuture<HouseResponse> createHouseAsync(@Valid HouseRequest houseRequest) {
        return CompletableFuture.completedFuture(createHouse(houseRequest));
    }

    @Async
    public CompletableFuture<HouseResponse> updateHouseAsync(@Valid HouseRequest houseRequest) {
        return CompletableFuture.completedFuture(updateHouse(houseRequest.getId(), houseRequest));
    }

    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(String id) {
        Boolean deleted = deleteHouse(id);
        return CompletableFuture.completedFuture(deleted);
    }
}
