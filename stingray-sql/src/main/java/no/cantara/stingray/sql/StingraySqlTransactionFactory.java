package no.cantara.stingray.sql;

import java.util.function.Consumer;
import java.util.function.Function;

public interface StingraySqlTransactionFactory {

    StingraySqlTransaction createTransaction() throws StingraySqlException;

    default void runInTransaction(Consumer<StingraySqlTransaction> work) throws StingraySqlException {
        try (StingraySqlTransaction transaction = createTransaction()) {
            work.accept(transaction);
            transaction.commit();
        }
    }

    default <R> R runInTransaction(Function<StingraySqlTransaction, R> work) throws StingraySqlException {
        try (StingraySqlTransaction transaction = createTransaction()) {
            R result = work.apply(transaction);
            transaction.commit();
            return result;
        }
    }
}
