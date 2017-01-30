package com.ecocarunicamp.ecoapp;


import android.os.AsyncTask;
import java.sql.ResultSet;
import java.sql.SQLException;
import android.util.Log;
import com.ecocarunicamp.ecoapp.dbMySQL;




public class SendMySql extends AsyncTask<String, Void, ResultSet> {
    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() */
    private dbMySQL dbmysql = new dbMySQL();
    private TabelaFuncMySql tmpTabela = new TabelaFuncMySql();
    private int escolhaExecucao = 1;
    private ClassTmpUsar tmpUsar;
    private int indiceTabela;

    protected ResultSet doInBackground(String... sql) {
        dbmysql.conectarMySQL(); // ip do servidor mysql, porta, banco, usuário, senha
        ResultSet retornoSQL = dbmysql.querysMySQL(sql[0]);
        return retornoSQL;
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
    protected void onPostExecute(ResultSet result) {
        try {
            switch (escolhaExecucao){
                case 1: naResposta(result);
                    break;
                case 2: tmpUsar.run(result);
                    break;
                case 3: tmpTabela.run(result);
                    break;
                default: naResposta(result);
            }

        }catch (Exception erro){
            Log.e("MYSQL","Erro: "+erro);
        }
        dbmysql.desconectarMySQL();
    }

    public void naResposta(ResultSet result) throws SQLException {
        //De um override nesta função com as ações a serem executadas com a resposta do SQL
    }

    public void usar(final MySqlRunnable obj){
        //Passe um objeto que implemente a ação desejada em run(ResultSet result)
        escolhaExecucao = 2;
        tmpUsar = new ClassTmpUsar(){
            @Override
            public void run(ResultSet result) throws SQLException {
                obj.run(result);
            }
        };
    }

    public void usarTabela(int escolha) {
        escolhaExecucao = 3;
        indiceTabela = escolha;
        tmpTabela.setIndice(escolha);
    }

    private class ClassTmpUsar implements MySqlRunnable{
        public void run (ResultSet result) throws SQLException{
            System.out.println("Função Não sobreescrita");
        }
    }



}
