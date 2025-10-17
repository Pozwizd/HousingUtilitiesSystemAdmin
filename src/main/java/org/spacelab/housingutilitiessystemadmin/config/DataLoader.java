package org.spacelab.housingutilitiessystemadmin.config;

import com.mongodb.bulk.BulkWriteResult;
import net.datafaker.Faker;
import org.spacelab.housingutilitiessystemadmin.entity.*;
import org.spacelab.housingutilitiessystemadmin.entity.location.*;
import org.spacelab.housingutilitiessystemadmin.service.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataLoader {

    private final UserService userService;
    private final RegionService regionService;
    private final ChairmanService chairmanService;
    private final ContactService contactService;
    private final ContactSectionService contactSectionService;
    private final BillService billService;
    private final VoteService voteService;
    private final AdminService adminService;
    private final MongoBulkConfig mongoBulkConfig;
    private final MongoTemplate mongoTemplate;

    public DataLoader(UserService userService,
                      RegionService regionService,
                      ChairmanService chairmanService,
                      ContactService contactService,
                      ContactSectionService contactSectionService,
                      BillService billService,
                      VoteService voteService,
                      AdminService adminService,
                      MongoBulkConfig mongoBulkConfig,
                      MongoTemplate mongoTemplate) {
        this.userService = userService;
        this.regionService = regionService;
        this.chairmanService = chairmanService;
        this.contactService = contactService;
        this.contactSectionService = contactSectionService;
        this.billService = billService;
        this.voteService = voteService;
        this.adminService = adminService;
        this.mongoBulkConfig = mongoBulkConfig;
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        Faker faker = new Faker(new Locale("ru"));

        clearAllData();

        adminService.createAdmin("admin@gmail.com", "admin@gmail.com");

        loadDataFromCsv();
        List<Chairman> chairmen = createChairmen(faker);
        mongoBulkConfig.bulkInsert(chairmen, Chairman.class);

        assignChairmenToHouses(faker, chairmen);

        List<User> users = createUsers(faker);
        mongoBulkConfig.bulkInsert(users, User.class);

        List<Bill> bills = createBills(faker);
        mongoBulkConfig.bulkInsert(bills, Bill.class);

        List<Contact> contacts = createContacts(faker);
        mongoBulkConfig.bulkInsert(contacts, Contact.class);

        List<ContactSection> contactSections = createContactSections(faker, contacts);
        mongoBulkConfig.bulkInsert(contactSections, ContactSection.class);

        List<Vote> votes = createVotes(faker, users);
        mongoBulkConfig.bulkInsert(votes, Vote.class);

        System.out.println("Data loading completed successfully!");
    }

    private void clearAllData() {
        userService.deleteAll();
        regionService.deleteAll();
        chairmanService.deleteAll();
        contactService.deleteAll();
        contactSectionService.deleteAll();
        billService.deleteAll();
        voteService.deleteAll();
    }

    public void loadDataFromCsv() {
        Map<String, Region> regionsMap = new HashMap<>();
        Map<String, City> citiesMap = new HashMap<>();
        Map<String, Street> streetsMap = new HashMap<>();
        List<House> allHouses = new ArrayList<>();

        try (InputStream is = new FileInputStream("streetBDtest.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(is, "Windows-1251"))) {

            br.readLine();
            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                String[] columns = line.split(";", -1);

                if (columns.length < 10) {
                    System.err.println("Недостаточно колонок в строке " + lineNumber + ": " + line);
                    continue;
                }

                try {
                    String regionName = columns[0].trim();
                    String cityName = columns[4].trim();
                    String postalCode = columns[5].trim();
                    String streetName = columns[6].trim();
                    String houseNumbers = columns[7].trim();
                    String statusText = columns[9].trim();

                    if (regionName.isEmpty() || cityName.isEmpty() || streetName.isEmpty()) {
                        continue;
                    }

                    Region region = regionsMap.computeIfAbsent(regionName, name -> {
                        Region newRegion = new Region();
                        newRegion.setName(name);
                        newRegion.setCities(new ArrayList<>());
                        return newRegion;
                    });

                    String cityKey = regionName + "_" + cityName;
                    City city = citiesMap.computeIfAbsent(cityKey, key -> {
                        City newCity = new City();
                        newCity.setName(cityName);
                        newCity.setRegion(null);
                        newCity.setStreets(new ArrayList<>());
                        return newCity;
                    });

                    String streetKey = cityKey + "_" + streetName;
                    Street street = streetsMap.computeIfAbsent(streetKey, key -> {
                        Street newStreet = new Street();
                        newStreet.setName(streetName);
                        newStreet.setCity(null);
                        newStreet.setHouses(new ArrayList<>());
                        return newStreet;
                    });

                    if (!houseNumbers.isEmpty()) {
                        String[] houses = houseNumbers.split(",");
                        for (String houseNum : houses) {
                            houseNum = houseNum.trim();
                            if (!houseNum.isEmpty()) {
                                String houseKey = streetKey + "_" + houseNum;

                                String finalHouseNum = houseNum;
                                boolean houseExists = allHouses.stream()
                                        .anyMatch(h -> h.getHouseNumber().equals(finalHouseNum) &&
                                                street.getHouses().contains(h));

                                if (!houseExists) {
                                    House house = new House();
                                    house.setHouseNumber(houseNum);
                                    house.setStreet(null);

                                    if (!statusText.isEmpty()) {
                                        house.setStatus(Status.DEACTIVATED);
                                    } else {
                                        house.setStatus(Status.ACTIVE);
                                    }

                                    allHouses.add(house);
                                    street.getHouses().add(house);
                                }
                            }
                        }
                    }

                    if (!region.getCities().contains(city)) {
                        region.getCities().add(city);
                    }

                    if (!city.getStreets().contains(street)) {
                        city.getStreets().add(street);
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка обработки строки " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Начинаем массовую вставку данных в MongoDB...");

            if (!allHouses.isEmpty()) {
                mongoBulkConfig.bulkInsert(allHouses, House.class);
                System.out.println("Вставлено домов: " + allHouses.size());
            }

            if (!streetsMap.isEmpty()) {
                for (Street street : streetsMap.values()) {
                    for (House house : street.getHouses()) {
                        house.setStreet(street);
                    }
                }

                List<Street> streets = new ArrayList<>(streetsMap.values());
                mongoBulkConfig.bulkInsert(streets, Street.class);
                System.out.println("Вставлено улиц: " + streets.size());
            }

            if (!citiesMap.isEmpty()) {
                for (City city : citiesMap.values()) {
                    for (Street street : city.getStreets()) {
                        street.setCity(city);
                    }
                }

                List<City> cities = new ArrayList<>(citiesMap.values());
                mongoBulkConfig.bulkInsert(cities, City.class);
                System.out.println("Вставлено городов: " + cities.size());
            }

            if (!regionsMap.isEmpty()) {
                for (Region region : regionsMap.values()) {
                    for (City city : region.getCities()) {
                        city.setRegion(region);
                    }
                }

                List<Region> regions = new ArrayList<>(regionsMap.values());
                mongoBulkConfig.bulkInsert(regions, Region.class);
                System.out.println("Вставлено регионов: " + regions.size());
            }

            System.out.println("Данные из CSV файла успешно загружены в MongoDB!");

            updateBidirectionalReferencesWithBulk(regionsMap, citiesMap, streetsMap);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки данных из CSV файла. Транзакция будет отменена.");
            throw new RuntimeException("Не удалось загрузить данные из CSV", e);
        }
    }


    private void updateBidirectionalReferencesWithBulk(Map<String, Region> regionsMap,
                                                       Map<String, City> citiesMap,
                                                       Map<String, Street> streetsMap) {
        System.out.println("Обновляем двусторонние связи через BulkOperations...");

        try {
            BulkOperations houseBulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, House.class);
            int houseUpdatesCount = 0;

            for (Street street : streetsMap.values()) {
                if (!street.getHouses().isEmpty()) {
                    List<String> houseIds = street.getHouses().stream()
                            .map(House::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    if (!houseIds.isEmpty()) {
                        houseBulkOps.updateMulti(
                                Query.query(Criteria.where("id").in(houseIds)),
                                Update.update("street", street)
                        );
                        houseUpdatesCount += houseIds.size();
                    }
                }
            }

            if (houseUpdatesCount > 0) {
                BulkWriteResult houseResult = houseBulkOps.execute();
                if (houseResult.wasAcknowledged()) {
                    System.out.println("Обновлены связи house.street: " + houseResult.getModifiedCount() + " из " + houseUpdatesCount);
                } else {
                    System.out.println("Операции house.street выполнены (неподтвержденная запись): " + houseUpdatesCount + " операций");
                }
            } else {
                System.out.println("Нет домов для обновления связей house.street");
            }

            BulkOperations streetBulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Street.class);
            int streetUpdatesCount = 0;

            for (City city : citiesMap.values()) {
                if (!city.getStreets().isEmpty()) {
                    List<String> streetIds = city.getStreets().stream()
                            .map(Street::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    if (!streetIds.isEmpty()) {
                        streetBulkOps.updateMulti(
                                Query.query(Criteria.where("id").in(streetIds)),
                                Update.update("city", city)
                        );
                        streetUpdatesCount += streetIds.size();
                    }
                }
            }

            if (streetUpdatesCount > 0) {
                BulkWriteResult streetResult = streetBulkOps.execute();
                if (streetResult.wasAcknowledged()) {
                    System.out.println("Обновлены связи street.city: " + streetResult.getModifiedCount() + " из " + streetUpdatesCount);
                } else {
                    System.out.println("Операции street.city выполнены (неподтвержденная запись): " + streetUpdatesCount + " операций");
                }
            } else {
                System.out.println("Нет улиц для обновления связей street.city");
            }

            BulkOperations cityBulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, City.class);
            int cityUpdatesCount = 0;

            for (Region region : regionsMap.values()) {
                if (!region.getCities().isEmpty()) {
                    List<String> cityIds = region.getCities().stream()
                            .map(City::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    if (!cityIds.isEmpty()) {
                        cityBulkOps.updateMulti(
                                Query.query(Criteria.where("id").in(cityIds)),
                                Update.update("region", region)
                        );
                        cityUpdatesCount += cityIds.size();
                    }
                }
            }

            if (cityUpdatesCount > 0) {
                BulkWriteResult cityResult = cityBulkOps.execute();
                if (cityResult.wasAcknowledged()) {
                    System.out.println("Обновлены связи city.region: " + cityResult.getModifiedCount() + " из " + cityUpdatesCount);
                } else {
                    System.out.println("Операции city.region выполнены (неподтвержденная запись): " + cityUpdatesCount + " операций");
                }
            } else {
                System.out.println("Нет городов для обновления связей city.region");
            }

            System.out.println("Все двусторонние связи успешно установлены через BulkOperations!");

        } catch (Exception e) {
            System.err.println("Ошибка при обновлении двусторонних связей: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось обновить двусторонние связи", e);
        }
    }


    private void assignChairmenToHouses(Faker faker, List<Chairman> chairmen) {
        List<Region> regions = regionService.findAll();
        List<Region> regionsToUpdate = new ArrayList<>();
        int houseCount = 0;

        for (Region region : regions) {
            boolean regionUpdated = false;
            for (City city : region.getCities()) {
                for (Street street : city.getStreets()) {
                    for (House house : street.getHouses()) {
                        if (!chairmen.isEmpty()) {
                            Chairman chairman = chairmen.get(faker.number().numberBetween(0, chairmen.size()));
                            house.setChairman(chairman);
                            houseCount++;
                            regionUpdated = true;
                        }
                    }
                }
            }
            if (regionUpdated) {
                regionsToUpdate.add(region);
            }
        }

        if (!regionsToUpdate.isEmpty()) {
            mongoBulkConfig.bulkUpsert(regionsToUpdate, Region.class);
            System.out.println("Bulk assigned chairmen to " + houseCount + " houses");
        }
    }


    private List<Chairman> createChairmen(Faker faker) {
        List<Chairman> chairmen = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Chairman chairman = new Chairman();
            chairman.setId(UUID.randomUUID().toString());
            chairman.setLastName(faker.name().lastName());
            chairman.setFirstName(faker.name().firstName());
            chairman.setMiddleName(faker.name().nameWithMiddle().split(" ")[1]);
            chairman.setPhone(faker.phoneNumber().phoneNumber());
            chairman.setEmail(faker.internet().emailAddress());
            chairman.setStatus(faker.options().option("active", "inactive"));
            chairman.setLogin(faker.internet().username());
            chairman.setPassword(faker.internet().password());

            File avatarFile = new File("uploads/avatar.jpg");
            if (avatarFile.exists() && avatarFile.isFile()) {
                chairman.setPhoto("uploads/avatar.jpg");
            }

            chairmen.add(chairman);
        }
        return chairmen;
    }


    private List<User> createUsers(Faker faker) {
        List<User> users = new ArrayList<>();
        List<Region> regions = regionService.findAll();
        List<HouseLocation> houseLocations = new ArrayList<>();

        for (Region region : regions) {
            for (City city : region.getCities()) {
                for (Street street : city.getStreets()) {
                    for (House house : street.getHouses()) {
                        houseLocations.add(new HouseLocation(house, city, street, region));
                    }
                }
            }
        }

        for (int i = 0; i < 200 && !houseLocations.isEmpty(); i++) {
            HouseLocation houseLocation = getRandomEntity(houseLocations);
            if (houseLocation == null) continue;

            User user = new User();
            user.setFirstName(faker.name().firstName());
            user.setMiddleName(faker.name().nameWithMiddle().split(" ")[1]);
            user.setLastName(faker.name().lastName());
            user.setPhone(faker.phoneNumber().phoneNumber());
            user.setEmail(faker.internet().emailAddress());
            user.setApartmentNumber(String.valueOf(faker.number().numberBetween(1, 200)));
            user.setApartmentArea(faker.number().randomDouble(2, 30, 150));
            user.setAccountNumber(faker.finance().iban());
            user.setStatus(getRandomEnum(Status.class));
            user.setPassword(faker.internet().password());
            File avatarFile = new File("uploads/avatar.jpg");
            if (avatarFile.exists() && avatarFile.isFile()) {
                user.setPhoto("uploads/avatar.jpg");
            }

            House userHouse = new House();
            userHouse.setId(houseLocation.house.getId());
            userHouse.setHouseNumber(houseLocation.house.getHouseNumber());
            userHouse.setStatus(houseLocation.house.getStatus());
            userHouse.setChairman(houseLocation.house.getChairman());

            City userCity = new City();
            userCity.setId(houseLocation.city.getId());
            userCity.setName(houseLocation.city.getName());

            Street userStreet = new Street();
            userStreet.setId(houseLocation.street.getId());
            userStreet.setName(houseLocation.street.getName());

            user.setHouse(userHouse);
            user.setCity(userCity);
            user.setStreet(userStreet);

            users.add(user);
        }
        return users;
    }

    private static class HouseLocation {
        final House house;
        final City city;
        final Street street;

        HouseLocation(House house, City city, Street street, Region region) {
            this.house = house;
            this.city = city;
            this.street = street;
        }
    }


    private List<Bill> createBills(Faker faker) {
        List<Bill> bills = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Bill bill = new Bill();
            bill.setId(UUID.randomUUID().toString());
            bill.setDate(LocalDate.now().minusDays(faker.number().numberBetween(1, 365)));
            bills.add(bill);
        }
        return bills;
    }

    private List<Contact> createContacts(Faker faker) {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Contact contact = new Contact();
            contact.setId(UUID.randomUUID().toString());
            contact.setFullName(faker.name().fullName());
            contact.setRole(faker.options().option("Manager", "Accountant", "Technician", "Administrator", "Engineer"));
            contact.setPhone(faker.phoneNumber().phoneNumber());
            contact.setPhoto(new byte[0]);
            contact.setDescription(faker.lorem().sentence());
            contacts.add(contact);
        }
        return contacts;
    }

    private List<ContactSection> createContactSections(Faker faker, List<Contact> contacts) {
        List<ContactSection> sections = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ContactSection section = new ContactSection();
            section.setId(UUID.randomUUID().toString());
            section.setTitle(faker.options().option("Management", "Technical Support", "Accounting", "Maintenance"));
            section.setContent(faker.lorem().paragraph());

            int contactCount = faker.number().numberBetween(2, 5);
            List<Contact> sectionContacts = new ArrayList<>();
            for (int j = 0; j < contactCount; j++) {
                sectionContacts.add(contacts.get(faker.number().numberBetween(0, contacts.size())));
            }
            section.setContacts(sectionContacts);

            sections.add(section);
        }
        return sections;
    }

    private List<Vote> createVotes(Faker faker, List<User> users) {
        List<Vote> votes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Vote vote = new Vote();
            vote.setId(UUID.randomUUID().toString());
            vote.setTitle(faker.lorem().sentence(3));
            vote.setDescription(faker.lorem().paragraph());
            vote.setStartTime(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, faker.number().numberBetween(1, 30));
            vote.setEndTime(calendar.getTime());
            vote.setQuorumArea(faker.number().randomDouble(2, 50, 100));
            vote.setStatus(faker.options().option("active", "completed", "pending"));

            int totalVoters = faker.number().numberBetween(3, 8);
            vote.setForVotesCount(faker.number().numberBetween(0, totalVoters));
            vote.setAgainstVotesCount(faker.number().numberBetween(0, totalVoters - vote.getForVotesCount()));
            vote.setAbstentionsCount(totalVoters - vote.getForVotesCount() - vote.getAgainstVotesCount());

            votes.add(vote);
        }
        return votes;
    }

    public <T> T getRandomEntity(Collection<T> collection) {
        if (collection.isEmpty()) {
            return null;
        }
        int index = new Random().nextInt(collection.size());
        List<T> list = new ArrayList<>(collection);
        return list.get(index);
    }

    private <T extends Enum<T>> T getRandomEnum(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            return null;
        }
        return enumConstants[new Random().nextInt(enumConstants.length)];
    }
}
