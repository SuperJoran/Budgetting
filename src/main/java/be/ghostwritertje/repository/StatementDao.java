package be.ghostwritertje.repository;

import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.budgetting.BankAccount;
import be.ghostwritertje.domain.budgetting.Category;
import be.ghostwritertje.domain.budgetting.Statement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Jorandeboever
 * Date: 01-Oct-16.
 */
@Repository
public interface StatementDao extends CrudRepository<Statement, String> {

    List<Statement> findByOriginatingAccount(BankAccount from);

    @Query(value = "SELECT COUNT(AMOUNT) FROM T_STATEMENT s " +
            "INNER JOIN T_BANKACCOUNT b ON s.ORIGINATINGACCOUNT_UUID = b.UUID " +
            "WHERE CATEGORY_UUID = ?1 AND b.ADMINISTRATOR_UUID = ?2", nativeQuery = true)
    Long findNumberOfStatementsForCategory(Category category, Person administrator);

    @Query(value = "SELECT COUNT(AMOUNT) FROM T_STATEMENT s " +
            "INNER JOIN T_BANKACCOUNT b ON s.ORIGINATINGACCOUNT_UUID = b.UUID " +
            "WHERE CATEGORY_UUID IS NULL AND b.ADMINISTRATOR_UUID = ?1", nativeQuery = true)
    Long findNumberOfStatementsWithoutCategory(Person administrator);

    @Query(value = "SELECT SUM(AMOUNT) FROM T_STATEMENT s " +
            "INNER JOIN T_BANKACCOUNT b ON s.ORIGINATINGACCOUNT_UUID = b.UUID " +
            "WHERE CATEGORY_UUID = ?1 AND b.ADMINISTRATOR_UUID = ?2 AND s.DATE > ?3 AND s.DATE < ?4" +
            "", nativeQuery = true)
    Double findSumOfStatementsByCategoryBetweenDates(Category category, Person person, LocalDate beginDate, LocalDate endDate);

    List<Statement> findByOriginatingAccount_Administrator(Person administrator);

}
