package be.ghostwritertje.services.budgetting;

import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.BankAccount;
import be.ghostwritertje.domain.budgetting.Category;
import be.ghostwritertje.domain.budgetting.Statement;
import be.ghostwritertje.repository.StatementDao;
import be.ghostwritertje.services.DomainObjectCrudServiceSupport;
import com.google.common.base.CharMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorandeboever
 * Date: 01-Oct-16.
 */
@Service
public class StatementServiceImpl extends DomainObjectCrudServiceSupport<Statement> implements StatementService {
    private final StatementDao dao;
    private final BankAccountService bankAccountService;

    @Autowired
    public StatementServiceImpl(StatementDao dao, BankAccountService bankAccountService) {
        this.dao = dao;
        this.bankAccountService = bankAccountService;
    }

    @Override
    protected CrudRepository<Statement, String> getDao() {
        return this.dao;
    }

    @Override
    public Long findNumberOfStatementsForCategory(Category category, Person administrator) {
        return this.dao.findNumberOfStatementsForCategory(category, administrator);
    }

    @Override
    public Long findNumberOfStatementsWithoutCategory(Person administrator) {
        return this.dao.findNumberOfStatementsWithoutCategory(administrator);
    }

    @Override
    public Iterable<Statement> save(Iterable<Statement> statements) {
        statements.forEach(statement -> {
            statement.setDescription(CharMatcher.whitespace().trimFrom(statement.getDescription()));
            statement.setCsvLine(CharMatcher.whitespace().trimFrom(statement.getCsvLine()));
        });
        return this.dao.save(statements);
    }

    @Override
    public List<Statement> findByAdministrator(Person administrator) {
        List<Statement> statements = new ArrayList<>();
        this.bankAccountService.findByAdministrator(administrator).forEach(bankAccount -> statements.addAll(this.dao.findByOriginatingAccount(bankAccount)));
        return statements;
    }

    @Override
    public List<Statement> findByOriginatingAccount(BankAccount bankAccount) {
        return this.dao.findByOriginatingAccount(bankAccount);
    }

    @Override
    public BigDecimal getTotal(BankAccount bankAccount) {
        return this.dao.findByOriginatingAccount(bankAccount).stream().map(Statement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotal(Person owner) {
        return this.bankAccountService.findByOwner(owner).stream().map(this::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
