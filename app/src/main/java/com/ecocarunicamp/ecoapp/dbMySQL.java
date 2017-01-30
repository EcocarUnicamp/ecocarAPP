package com.ecocarunicamp.ecoapp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import android.app.Activity;

import android.util.Log;public class dbMySQL {

    private Connection conn = null;
    private Statement st;
    private ResultSet rs;
    //private String sql;

    public void conectarMySQL(){


        //server preencher com dados do banco de dados externo
		
        String host = MACHOST;
        String banco = NOMEDB;
        String usuario = USER;
        String senha = SENHA;


        String porta = NUMPORTA;

        System.out.println("ECOCAR: Configurou variaveis de URI do banco");
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            System.out.println("ECOCAR:Instanciou driver JDBC");
        }catch(Exception erro){
            Log.e("MYSQL","Erro: "+erro);
        }
        try{
            System.out.println("ECOCAR: Tentando abrir conexão...");
            conn=DriverManager.getConnection("jdbc:mysql://"+host+":"+porta+"/"+banco,usuario,senha);
            System.out.println("ECOCAR: Conectado!");
            Log.i("MYSQL","Conectado.");
        }catch(Exception erro){
            Log.e("MYSQL","Erro: "+erro);
        }
    }

    public void desconectarMySQL(){
        try {
            conn.close();
            Log.i("MYSQL","Desconectado.");
        } catch (Exception erro) {
            Log.e("MYSQL","Erro: "+erro);
        }
    }

    public ResultSet querysMySQL(String sql){
        try{
            st=conn.createStatement();
            rs=st.executeQuery(sql);

        } catch (Exception erro){
            Log.e("MYSQL","Erro: "+erro);
        }
        return rs;
    }


    public int updatesMySQL(String sql){
        try{
            st=conn.createStatement();
            return st.executeUpdate(sql);

        } catch (Exception erro){
            Log.e("MYSQL","Erro: "+erro);
            return 0;
        }

    }

}
