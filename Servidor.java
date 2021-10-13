/*
    Distribuido de manera abierta
 */
import java.net.*;
import java.io.*;
import java.util.zip.*;

/**
 *
 * @author angel & samuel
 */
public class Servidor {
    ServerSocket Servidor;
    int puerto=8081;
    String raiz="./Nube";

    public void Atender_Peticion(Socket Cliente){
        try {
            DataInputStream dis=new DataInputStream(Cliente.getInputStream());
            //Leer peticion
            String Peticion=dis.readUTF();
            dis.close();
            switch(Peticion.charAt(0)){
                case 'd':
                    Comando_Descargar(Peticion.substring(2),Cliente);
                    break;
    
                case 'a':
                    Comando_Abrir(Peticion.substring(2),Cliente); //Mostrar contenido devuelto
                    break;
    
                case 'e':
                    Comando_Eliminar(Peticion.substring(2),Cliente); //Esperar confirmación
                    break;
    
                case 's':
                    Comando_Enviar(Peticion.substring(2),Cliente);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Comando_Enviar(String direccion_relativa, Socket Cliente) {
        try {
            Cliente=Servidor.accept();
            System.out.println("Preparandoce para recibir de "+Cliente.getPort()+"."+Cliente.isClosed());
            DataInputStream dis=new DataInputStream(Cliente.getInputStream());
            String nombre=dis.readUTF();
            long tamanio=dis.readLong();
            String aux=new File("").getAbsolutePath()+"/Nube/"+nombre;
            System.out.println("aux:"+aux);
            DataOutputStream dos=new DataOutputStream(new FileOutputStream(aux));
            long recibidos=0;
            int l=0,porcentaje=0;
            while(recibidos<tamanio){
                byte[] b = new byte[3000];
                l=dis.read(b);
                System.out.println("recibidos: "+l);
                dos.write(b,0,l);
                dos.flush();
                recibidos =recibidos + l;
                porcentaje = (int)((recibidos*100)/tamanio);
                System.out.print("Recibido "+porcentaje+" % del archivo");
            }
            System.out.println("\nArchivo recibido...");
            dis.close();
            dos.close();
            Cliente.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void Comando_Descargar(String direccion_relativa, Socket Cliente){
        try {
            System.out.println("Conectando con el servidor");
            Cliente = Servidor.accept();
            System.out.println("Conectado");
            File f=new File(raiz+direccion_relativa);
            if(f.isDirectory()){
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("./"+f.getName()+".zip ")); 
                Comprimir(f.getAbsolutePath(), zos); 
                zos.close();
                f= new File( "./" + f.getName() + ".zip " );
            }
            String nombre=f.getName();
            String direccion=f.getAbsolutePath();
            long tamanio=f.length();
            
            DataOutputStream dos = new DataOutputStream(Cliente.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(direccion));
            dos.writeUTF(nombre);
            dos.flush();
            dos.writeLong(tamanio);
            dos.flush();
            long enviados = 0;
            int l=0,porcentaje=0;
            while(enviados<tamanio){
                byte[] b = new byte[1500];
                l=dis.read(b);
                System.out.println("enviados: "+l);
                dos.write(b,0,l);
                dos.flush();
                enviados = enviados + l;
                porcentaje = (int)((enviados*100)/tamanio);
                System.out.println("\rEnviado "+porcentaje+" % del archivo");
            }
            System.out.println("\nArchivo enviado...");
            dis.close();
            dos.close();
            Cliente.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Comando_Eliminar(String direccion_relativa,Socket Cliente){
        try {
            System.out.println("Conectando con el servidor");
            Cliente = Servidor.accept();
            System.out.println("Conectado");
            String direccion_absoluta=raiz+direccion_relativa;
            File f=new File(direccion_absoluta);
            if(f.isDirectory())
                VaciarCarpeta(f.getAbsolutePath());
            f.delete();
            DataOutputStream dos=new DataOutputStream(Cliente.getOutputStream());
            dos.writeBoolean(true);
            dos.close();
            Cliente.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Comando_Abrir(String direccion_relativa,Socket Cliente) {
        try {

            Cliente=Servidor.accept();
            DataOutputStream dos=new DataOutputStream(Cliente.getOutputStream());
            if(direccion_relativa.contains("..")){
                dos.writeInt(0);
                Cliente.close();
                return;
            }
            DataInputStream dis=new DataInputStream(Cliente.getInputStream());
            File f=new File(raiz+direccion_relativa);
            if(!f.isDirectory()){
                dos.writeInt(0);
                Cliente.close();
                return;
            }
            dos.writeInt(f.list().length);
            dos.flush();
            for (String archivo:f.list()) {
                dos.writeUTF(archivo);
                dos.flush();
                
            }
            Cliente.close();
            dos.close();
            dis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void VaciarCarpeta(String Objetivo) {
        File aux;
        for (String archivo:new File(Objetivo).list()) {
            aux=new File(Objetivo+"/"+archivo);
            if (aux.isDirectory())
                VaciarCarpeta(aux.getAbsolutePath());
            aux.delete();
        }
        return;
    }

    public static void main(String[] args) {
        new Servidor().Iniciar();
    }

    public void Iniciar(){
        try{
            Servidor = new ServerSocket(puerto);
            Socket Cliente_aux;
            while(true){
                System.out.println("Esperando cliente");
                Cliente_aux=Servidor.accept();
                Atender_Peticion(Cliente_aux);
                System.out.println("Cliente atendido");
            }
        }catch(Exception e ){
            e.printStackTrace();
        }
    }

    public void Comprimir (String destino, ZipOutputStream flujo_salida){
        final int buffer_size=2156;
        try {   
            File directorio=new File(destino);
            String[] dirList = directorio.list(); 
            byte[] readBuffer = new byte[buffer_size]; 
            int bytesIn = 0;  
            for(String nombreArchivo:dirList){//trevel in each element
                File destino_compresion_aux=new File(destino, nombreArchivo);
                if(destino_compresion_aux.isDirectory())
                    Comprimir(destino_compresion_aux.getPath(), flujo_salida);
                else{
                    FileInputStream fis = new FileInputStream(destino_compresion_aux); 
                    flujo_salida.putNextEntry(new ZipEntry(destino_compresion_aux.getPath())); 
                    while((bytesIn = fis.read(readBuffer))>0)
                       flujo_salida.write(readBuffer, 0, bytesIn); 
                    fis.close(); 
                }             
            }
        }catch(Exception e){ 

        } 
    }

}

