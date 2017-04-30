package be.ghostwritertje.services.budgetting;

import be.ghostwritertje.domain.DomainObject;
import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.BankAccount;
import be.ghostwritertje.domain.budgetting.Category;
import be.ghostwritertje.domain.budgetting.CategoryGroup;
import be.ghostwritertje.domain.budgetting.Statement;
import be.ghostwritertje.repository.CategoryDao;
import be.ghostwritertje.services.DomainObjectCrudServiceSupport;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jorandeboever
 * Date: 17-Apr-17.
 */
@Service
public class CategoryServiceImpl extends DomainObjectCrudServiceSupport<Category> implements CategoryService {

    private final CategoryDao categoryDao;
    private final BankAccountService bankAccountService;
    private final StatementService statementService;
    private final CategoryGroupService categoryGroupService;

    @Autowired
    public CategoryServiceImpl(
            CategoryDao categoryDao,
            BankAccountService bankAccountService,
            StatementService statementService,
            CategoryGroupService categoryGroupService
    ) {
        this.categoryDao = categoryDao;
        this.bankAccountService = bankAccountService;
        this.statementService = statementService;
        this.categoryGroupService = categoryGroupService;
    }

    @Override
    public void initForNewPerson(Person person) {
        Collection<Category> categories = new ArrayList<>();
        categories.add(new Category("Saving"));
        categories.add(new Category("Car & Transport"));
        categories.add(new Category("Housing"));
        categories.add(new Category("Groceries"));
        categories.add(new Category("Entertainment"));
        categories.add(new Category("Internal"));
        categories.add(new Category("Medical"));
        categories.add(new Category("Telecom"));
        categories.add(new Category("Insurance"));
        categories.add(new Category("Taxes"));
        categories.add(new Category("Energy"));

//        categories.forEach(category -> category.setAdministrator(person));

        this.save(categories);
    }

    @Override
    public void attemptToAssignCategoriesAutomaticallyForPerson(Person person) {
        List<Statement> statements = this.statementService.findByAdministrator(person);
        List<Category> categories = this.findByAdministrator(person);

        Map<String, BankAccount> bankAccountMap = this.bankAccountService.findByOwner(person).stream().collect(Collectors.toMap(DomainObject::getUuid, b -> b));

        categories.stream()
                .filter(category -> "internal".equalsIgnoreCase(category.getName()))
                .findFirst()
                .ifPresent(category -> {
                    statements.stream()
                            .filter(statement -> Optional.ofNullable(statement.getDestinationAccount())
                                    .map(bankAccount -> bankAccountMap.containsKey(bankAccount.getUuid()))
                                    .orElse(false))
                            .forEach(statement -> statement.setCategory(category));
                });

        this.statementService.save(statements);
    }

    @Override
    public void delete(Category object) {
        this.categoryDao.delete(object);
    }

    @Override
    public Category save(Category object) {
        return this.categoryDao.save(object);
    }

    @Override
    public List<Category> findByCategoryGroup(CategoryGroup categoryGroup) {
        return this.categoryDao.findByCategoryGroup(categoryGroup);
    }

    public List<Category> findByAdministrator(Person administrator) {
        return this.categoryGroupService.findByAdministrator(administrator).stream()
                .map(this::findByCategoryGroup)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Category, Long> findCountByAdministrator(Person administrator) {
        Map<Category, Long> result = this.findByAdministrator(administrator).stream()
                .collect(Collectors.toMap(
                        category -> category,
                        category -> this.statementService.findNumberOfStatementsForCategory(category, administrator)
                ));

        result.put(new Category("None"), this.statementService.findNumberOfStatementsWithoutCategory(administrator));

        return Maps.filterValues(result, count -> count != null && count != 0);

    }

    @Override
    public Iterable<Category> save(Iterable<Category> categories) {
        return this.categoryDao.save(categories);
    }

    @Override
    protected CrudRepository<Category, String> getDao() {
        return this.categoryDao;
    }
}
