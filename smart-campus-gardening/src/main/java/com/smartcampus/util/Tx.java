package com.smartcampus.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runs several DAO calls in ONE database transaction: all succeed (commit) or all are undone (rollback).
 * Usage: Tx.run(conn -> { dao.insert(conn, ...); logDao.insert(conn, ...); });
 */
public final class Tx {
    @FunctionalInterface
    public interface Work { void run(Connection c) throws SQLException; }

    private Tx() { }

    public static void run(Work work) {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                work.run(c);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException ignored) { }
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new DataAccessException("Transaction failed", e);
            } finally {
                try { c.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not open connection", e);
        }
    }
}
