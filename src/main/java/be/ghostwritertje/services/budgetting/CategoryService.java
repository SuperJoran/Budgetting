package be.ghostwritertje.services.budgetting;

import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.Category;
import be.ghostwritertje.domain.budgetting.CategoryGroup;
import be.ghostwritertje.domain.budgetting.CategoryType;
import be.ghostwritertje.services.DomainObjectCrudService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Jorandeboever
 * Date: 17-Apr-17.
 */
public interface CategoryService extends DomainObjectCrudService<Category> {
    List<Category> findByCategoryGroup(CategoryGroup categoryGroup);

    List<Category> findByAdministrator(Person administrator);

    Map<Category, Long> findCountByAdministrator(Person administrator);

    Map<Category, BigDecimal> findSumByAdministrator(Person administrator, CategoryType categoryType);

    Iterable<Category> save(Iterable<Category> categories);

    void initForNewPerson(Person person);

    void attemptToAssignCategoriesAutomaticallyForPerson(Person person);
}
