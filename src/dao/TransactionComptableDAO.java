package dao;

import model.TransactionComptable;
import java.sql.Connection; 
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionComptableDAO {
    boolean addTransaction(TransactionComptable transaction) throws SQLException;
    boolean addTransaction(Connection conn, TransactionComptable transaction) throws SQLException;

    TransactionComptable getTransactionById(int id) throws SQLException;
    List<TransactionComptable> getAllTransactions() throws SQLException;
    List<TransactionComptable> getTransactionsByCompte(int idCompte) throws SQLException;
    List<TransactionComptable> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException;

    List<TransactionComptable> getTransactionsBySourceType(String sourceType) throws SQLException;
    boolean deleteTransaction(int id) throws SQLException;
}
