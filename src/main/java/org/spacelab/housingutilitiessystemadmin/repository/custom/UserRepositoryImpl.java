package org.spacelab.housingutilitiessystemadmin.repository.custom;

import lombok.RequiredArgsConstructor;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<User> findUsersWithFilters(UserRequestTable userRequestTable) {
        Query query = buildFilterQuery(userRequestTable);

        long totalCount = mongoTemplate.count(query, User.class);

        Pageable pageable = PageRequest.of(
                userRequestTable.getPage(),
                userRequestTable.getSize(),
                Sort.by(Sort.Direction.ASC, "lastName", "firstName")
        );

        query.with(pageable);

        List<User> users = mongoTemplate.find(query, User.class);

        return PageableExecutionUtils.getPage(users, pageable, () -> totalCount);
    }

    private Query buildFilterQuery(UserRequestTable filter) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.hasText(filter.getFullName())) {
            String namePattern = filter.getFullName().trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("firstName").regex(namePattern, "i"),
                    Criteria.where("middleName").regex(namePattern, "i"),
                    Criteria.where("lastName").regex(namePattern, "i")
            ));
        }

        if (StringUtils.hasText(filter.getPhoneNumber())) {
            criteriaList.add(Criteria.where("phone").regex(filter.getPhoneNumber(), "i"));
        }

        if (StringUtils.hasText(filter.getHouseNumber())) {
            criteriaList.add(Criteria.where("houseNumber").is(filter.getHouseNumber()));
        }

        if (StringUtils.hasText(filter.getApartmentNumber())) {
            criteriaList.add(Criteria.where("apartmentNumber").is(filter.getApartmentNumber()));
        }

        if (StringUtils.hasText(filter.getAccountNumber())) {
            criteriaList.add(Criteria.where("accountNumber").regex(filter.getAccountNumber(), "i"));
        }

        if (filter.getStatus() != null) {
            criteriaList.add(Criteria.where("status").is(filter.getStatus().toString()));
        }

        if (StringUtils.hasText(filter.getCityName())) {
            criteriaList.add(buildCityNameCriteria(filter.getCityName()));
        }

        if (StringUtils.hasText(filter.getStreetName())) {
            criteriaList.add(buildStreetNameCriteria(filter.getStreetName()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])
            ));
        }

        return query;
    }

    private Criteria buildCityNameCriteria(String cityName) {
        Query cityQuery = new Query(Criteria.where("name").regex(cityName, "i"));
        List<String> cityIds = mongoTemplate.find(cityQuery, org.bson.Document.class, "cities")
                .stream()
                .map(doc -> doc.getObjectId("_id").toString())
                .toList();

        if (cityIds.isEmpty()) {
            return Criteria.where("city.$id").in(List.of());
        }

        return Criteria.where("city.$id").in(cityIds);
    }

    private Criteria buildStreetNameCriteria(String streetName) {
        Query streetQuery = new Query(Criteria.where("name").regex(streetName, "i"));
        List<String> streetIds = mongoTemplate.find(streetQuery, org.bson.Document.class, "streets")
                .stream()
                .map(doc -> doc.getObjectId("_id").toString())
                .toList();

        if (streetIds.isEmpty()) {
            return Criteria.where("street.$id").in(List.of());
        }

        return Criteria.where("street.$id").in(streetIds);
    }
}
