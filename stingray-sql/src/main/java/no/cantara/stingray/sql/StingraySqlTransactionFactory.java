package no.cantara.stingray.sql;

import java.util.function.Consumer;

public interface StingraySqlTransactionFactory {

    StingraySqlTransaction createTransaction() throws StingraySqlException;

    default void runInTransaction(Consumer<StingraySqlTransaction> work) throws StingraySqlException {
        try (StingraySqlTransaction transaction = createTransaction()) {
            work.accept(transaction);
            transaction.commit();
        }
    }
}
