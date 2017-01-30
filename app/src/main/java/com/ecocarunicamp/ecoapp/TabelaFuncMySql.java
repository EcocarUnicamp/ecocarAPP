package com.ecocarunicamp.ecoapp;

import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by BalaPC on 06-Jun-16.
 */
public class TabelaFuncMySql {
    //Classe que mantem uma tabela de scripts que podem ser usados na chamada MySql.
    private int indice;
    public TabelaFuncMySql(){}
    public TabelaFuncMySql(int valor){
        setIndice(valor);
    }

    public void run(ResultSet result) throws SQLException{
        switch (indice){
            //Prencher com as funções e seus indices
            case 1: script1(result);
                break;





            default: System.out.println("Classe TabelaFuncMySql ficou sem definição de índice");
        }
    }


    private void script1(ResultSet result) throws SQLException {
        result.first();
        do {
            Log.i("MYSQL", "Resultado: " + result.getString("nome"));
        }while(result.next());
    }




    public void setIndice(int indice) {
        this.indice = indice;
    }








}
