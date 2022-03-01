package no.cantara.stingray.sql;

public interface StingraySqlTransactionFactory {

    StingraySqlTransaction createTransaction() throws StingraySqlException;
}
