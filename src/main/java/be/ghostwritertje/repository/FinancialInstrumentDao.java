package be.ghostwritertje.repository;

import be.ghostwritertje.domain.Person;
import be.ghostwritertje.domain.investing.FinancialInstrument;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Jorandeboever
 * Date: 15-Oct-16.
 */
@Repository
public interface FinancialInstrumentDao extends CrudRepository<FinancialInstrument, String> {
    FinancialInstrument findByQuote(String quote);

    @Query(value = "SELECT * FROM T_FINANCIAL_INSTRUMENT A LEFT JOIN T_HISTORIC_PRICE B ON B.FINANCIAL_INSTRUMENT_ID = A.ID WHERE B.ID IS NULL", nativeQuery = true)
    List<FinancialInstrument> findFinancialInstrumentsWithoutHistory();

    @Query(value = "SELECT DISTINCT i.QUOTE FROM T_FINANCIAL_INSTRUMENT i\n" +
            "INNER JOIN T_FUND_PURCHASE purchase on i.QUOTE = purchase.QUOTE\n" +
            "INNER JOIN T_PERSON person on purchase.PERSON_UUID = person.UUID\n" +
            "WHERE person.USERNAME = ?1", nativeQuery = true)

    List<FinancialInstrument> findFinancialInstrumentsByPerson(Person person);

}
