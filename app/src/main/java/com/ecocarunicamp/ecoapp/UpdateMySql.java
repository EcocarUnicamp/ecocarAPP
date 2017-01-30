package com.ecocarunicamp.ecoapp;



import android.content.res.Resources;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.sql.ResultSet;
import java.sql.SQLException;

import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.graphics.Bitmap;
import 	android.graphics.BitmapFactory;
import android.widget.ImageView;


import com.ecocarunicamp.ecoapp.dbMySQL;
import com.ecocarunicamp.ecoapp.R;
import com.ecocarunicamp.ecoapp.UsuarioSingleton;

/**
 * Created by BalaPC on 03-Jun-16.
 *
 SendfeedbackJob job = new SendfeedbackJob();
 job.execute();
 dbMySQL dbmysql = new dbMySQL();
 dbmysql.conectarMySQL("127.0.0.1", "3306", "test", "root", "bala123"); // ip do servidor mysql, porta, banco, usuário, senha
 dbmysql.queryMySQL();
 dbmysql.desconectarMySQL();
 *
 */



public class UpdateMySql extends AsyncTask<String, Void, Integer> {
    /** The system calls this to perform work in a worker thread and
     * delivers it the parameters given to AsyncTask.execute() */
    private dbMySQL dbmysql = new dbMySQL();
    private TabelaFuncMySql tmpTabela = new TabelaFuncMySql();
    private int escolhaExecucao = 1;
    //private ClassTmpUsar tmpUsar;
    private int indiceTabela;

    protected Integer doInBackground(String... sql) {
        Integer resposta;
        dbmysql.conectarMySQL(); // ip do servidor mysql, porta, banco, usuário, senha
        resposta =  dbmysql.updatesMySQL(sql[0]);
        return resposta;
    }

    /** The system calls this to perform work in the UI thread and delivers
     * the result from doInBackground() */
    protected void onPostExecute(Integer result) {
        try {
            naResposta(result);
        }catch (Exception erro){
            Log.e("MYSQL","Erro: "+erro);
        }
        dbmysql.desconectarMySQL();
    }

    public void naResposta(Integer result) throws SQLException {
        //De um override nesta função com as ações a serem executadas com a resposta do SQL
    }



    /////////////////////////////////////////////////////////////////

    static public void sendPicture(Bitmap bitmap, int event_id){
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
        //Resources res = getContext().getResources();
        //Bitmap bitmap = BitmapFactory.decodeResource(bitmap2, R.drawable.cadastro);
        //System.out.println(bitmap.toString());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte [] byte_arr = stream.toByteArray();
        String image_str = Base64.encodeToString(byte_arr, Base64.DEFAULT);
        enviaImagemServidor(image_str,Integer.toString(event_id));
    }

    static public Bitmap foto;
    //static public int adfoto;

    static public void retrievePicture(int id, final UpdateMySql tmp){

        //////////////
        SendMySql job1;
        job1 = new SendMySql(){
            @Override
            public void naResposta(ResultSet result) throws SQLException {
                while(result.next()) {

                    //UpdateMySql tmp = new UpdateMySql();
                    System.out.println("Foto recebida");
                    String imagem = result.getString("foto");
                    byte [] byte_arr = Base64.decode(imagem,Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(byte_arr , 0, byte_arr .length);
                    UpdateMySql.foto = bitmap;
                    tmp.recebeImagem(bitmap);
                    System.out.println("Imagem Recebida e convertida em bitmap");
                }
            }
        };

        String sql = "SELECT * FROM fotos WHERE `evento_id` = '"+ Integer.toString(id)+"'";
        job1.execute(sql);

    }

    public void recebeImagem(Bitmap imagem){

        //ImageView img = R.findViewById(R.id.imageView);
        //img.setImageBitmap(imagem);

    }


    static private boolean enviaImagemServidor(String img, String event_id){
        UpdateMySql job2;
        job2 = new UpdateMySql(){
            @Override
            public void naResposta(Integer result) throws SQLException {
                if(result > 0) {
                    System.out.println("Foto Enviada");
                }else{
                    System.out.println("Foto não enviada");
                }
            }
        };
        UsuarioSingleton user = UsuarioSingleton.getInstance();
        String sql = "INSERT INTO fotos  (`dono`,`evento_id`,`foto`) VALUES ('" +
                user.getUsuario()+
                "','" +
                event_id +
                "','" +
                img +
                "')";

        job2.execute(sql);
        return true;
    }

}
