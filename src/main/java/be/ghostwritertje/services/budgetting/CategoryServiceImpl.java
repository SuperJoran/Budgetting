package be.ghostwritertje.services.budgetting;

import be.ghostwritertje.domain.DomainObject;
import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.BankAccount;
import be.ghostwritertje.domain.budgetting.Category;
import be.ghostwritertje.domain.budgetting.CategoryGroup;
import be.ghostwritertje.domain.budgetting.CategoryType;
import be.ghostwritertje.domain.budgetting.Statement;
import be.ghostwritertje.repository.CategoryDao;
import be.ghostwritertje.repository.CategoryViewDao;
import be.ghostwritertje.services.DomainObjectCrudServiceSupport;
import be.ghostwritertje.services.NumberDisplay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private CategoryViewDao categoryViewDao;

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
        CategoryGroup car = new CategoryGroup("Car & Transport");
        car.addCategory(new Category("Fuel"));
        car.addCategory(new Category("Taxes"));
        car.addCategory(new Category("Insurance"));
        car.addCategory(new Category("Upkeep"));

        CategoryGroup household = new CategoryGroup("Household");
        household.addCategory(new Category("Food"));

        CategoryGroup healthcare = new CategoryGroup("Healthcare");
        healthcare.addCategory(new Category("Docter"));
        healthcare.addCategory(new Category("Pharmacy"));
        healthcare.addCategory(new Category("Mutuality"));

        CategoryGroup clothingAndPersonal = new CategoryGroup("Clothing & Personal Care");
        clothingAndPersonal.addCategory(new Category("Hairdresser"));
        clothingAndPersonal.addCategory(new Category("Clothing"));

        CategoryGroup entertainment = new CategoryGroup("Entertainment");
        entertainment.addCategory(new Category("Games"));
        entertainment.addCategory(new Category("Movies"));

        CategoryGroup telecom = new CategoryGroup("Telecom");
        telecom.addCategory(new Category("Phone"));
        telecom.addCategory(new Category("Subscription"));

        CategoryGroup other = new CategoryGroup("Other");
        other.addCategory(new Category("Internal"));

        List<CategoryGroup> categoryGroupList = Arrays.asList(
                car,
                new CategoryGroup("Housing"),
                telecom,
                clothingAndPersonal,
                healthcare,
                entertainment,
                household,
                other
        );

        categoryGroupList.forEach(categoryGroup -> categoryGroup.setAdministrator(person));

        this.categoryGroupService.save(categoryGroupList);
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

        statements.stream()
                .filter(statement -> Optional.ofNullable(statement.getDestinationAccount())
                        .map(bankAccount -> bankAccountMap.containsKey(bankAccount.getUuid()) && bankAccountMap.get(bankAccount.getUuid()).getCategory() != null)
                        .orElse(false))
                .forEach(statement -> statement.setCategory(bankAccountMap.get(statement.getDestinationAccount().getUuid()).getCategory()));

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
    public List<NumberDisplay> findCountByAdministrator(Person administrator) {
        List<NumberDisplay> result = this.findByAdministrator(administrator).stream()
                .map(category -> new NumberDisplayImpl(
                        category.getName(),
                        BigDecimal.valueOf(this.statementService.findNumberOfStatementsForCategory(category, administrator))
                ))
                .filter(numberDisplay -> numberDisplay.getNumberDisplayValue() != null && numberDisplay.getNumberDisplayValue().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());

        result.add(new NumberDisplayImpl("None", BigDecimal.valueOf(this.statementService.findNumberOfStatementsWithoutCategory(administrator))));

        return result;

    }

    @Override
    public List<NumberDisplay> findSumByAdministrator(Person administrator, CategoryType categoryType) {
        LocalDate beginDate = LocalDate.of(LocalDate.now().getYear() - 1, 1, 1);
        LocalDate endDate = beginDate.plusYears(1);
        List<NumberDisplay> result = this.findByAdministrator(administrator).stream()
                .filter(category -> category.getCategoryGroup().getCategoryType() == categoryType)
                .sorted(Comparator.comparing(category -> category.getCategoryGroup().getName()))
                .map(category -> new NumberDisplayImpl(
                        category.getName(),
                        Optional.ofNullable(this.categoryViewDao.findOne(category.getUuid()).getAmount()).orElseGet(() -> BigDecimal.ZERO).abs()
                ))
                .filter(numberDisplay -> numberDisplay.getNumberDisplayValue() != null && numberDisplay.getNumberDisplayValue().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());
//                .collect(Collectors.toMap(
//                        category -> category,
//                        category -> Optional.ofNullable(this.categoryViewDao.findOne(category.getUuid()).getAmount()).orElseGet(() -> BigDecimal.ZERO).abs()
//                ));
//        result.put(new Category("None"), this.statementService.findNumberOfStatementsWithoutCategory(administrator));

//        return Maps.filterValues(result, sum -> sum != null && sum.compareTo(BigDecimal.ZERO) != 0);
        return result;
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
