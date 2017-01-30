package com.ecocarunicamp.ecoapp;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface MySqlRunnable {
    public void run(ResultSet result) throws SQLException;
}
