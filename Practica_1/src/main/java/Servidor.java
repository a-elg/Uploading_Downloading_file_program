/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.*;
import java.io.*;

/**
 *
 * @author angel & samuel
 */
public class Servidor {
    
    public static void main(String[] args){
        ObjectOutputStream oos = null;
        ObjectInputStream ois= null;
      try{
        ServerSocket s = new ServerSocket(9999);
        System.out.println("Servicio iniciado... Esperando cliente");
        for(;;){
            //Accept the client conection
            Socket cl= s.accept();
            System.out.println("Cliente conectado desde "+cl.getInetAddress()+":"+cl.getPort());
            //Create 2 sockets, 1 to recive info about the user and the other to send feedback bakc to the user
            oos= new ObjectOutputStream(cl.getOutputStream());
            ois = new ObjectInputStream(cl.getInputStream());
            //Create an User object to later cast the sent object into it
            User u=(User)ois.readObject();
            //this user exist? and if it is the case, did the user had the correct password?
            try {
                File user_info=new File(new File("").getAbsolutePath()+"\\");
            } catch (Exception e) {
                //TODO: handle exception
            }
            boolean bool = f1.mkdir();  
            if(bool){  
               System.out.println("Folder is created successfully");  
            }else{  
               System.out.println("Error Found!");  
            }  
            oos.writeObject();
            oos.flush();
            
        }//for
        
      }  catch(Exception e){
          e.printStackTrace();
      }//catch
    }//main
}//class

