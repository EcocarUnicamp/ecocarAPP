package com.ecocarunicamp.ecoapp;


public class UsuarioSingleton {
    private static UsuarioSingleton usuario;
    private String name = "Teste user";

    public UsuarioSingleton(){

    }

    public static synchronized  UsuarioSingleton getInstance(){

        if(usuario == null) {
            usuario = new UsuarioSingleton();
        }
        return usuario;

    }

    public String getUsuario(){

        return this.name;
    }

}
