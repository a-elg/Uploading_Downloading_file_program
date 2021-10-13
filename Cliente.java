import java.net.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.zip.*;
import java.io.*;
import javax.swing.JFileChooser;

public class Cliente {
    Socket cl;
        String direccion="127.0.0.1";
        int puerto=8081;
    DataInputStream dis;
    DataOutputStream dos;
    ObjectInputStream ois;
    String descargas;
    String directorio_concurrente;

    Scanner sc=new Scanner(System.in);
    JFileChooser jf;

    public void ComandoRemoto(String comando){
        try{
            cl = new Socket(direccion,puerto);
            dos = new DataOutputStream(cl.getOutputStream() );
            dos.writeUTF(comando);      
            dos.close();
            cl.close();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    public boolean Procesar(String comando) {
        try {
            if(comando.charAt(0)=='c'){
                System.out.println("Adios~");
                System.exit(1);
            }
            else if(comando.charAt(1)!=' ')
                return true;

            switch (comando.charAt(0)) {
                case 'd':
                    ComandoRemoto(comando);
                    Descargar();
                    break;

                case 'a':
                    ComandoRemoto(comando);
                    Abrir(comando.substring(2));
                    break;

                case 'e':
                    ComandoRemoto(comando);
                    Eliminar();
                    break;

                case 's':
                    ComandoRemoto(comando);
                    Enviar();
                    break;

                default:
                    return true;
            }
            return false; 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void Enviar() {
        try {
            cl = new Socket(direccion,puerto);
            JFileChooser jf = new JFileChooser();
            jf.setFileSelectionMode(2);
            //jf.setMultiSelectionEnabled(true);
            int r=jf.showOpenDialog(null);
            if(r==JFileChooser.APPROVE_OPTION){
                File f=jf.getSelectedFile();
                if(f.isDirectory()){
                    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("./"+f.getName()+".zip ")); 
                    Comprimir(f.getAbsolutePath(), zos); 
                    zos.close();
                    f= new File( "./" + f.getName() + ".zip " );
                }
                String nombre=f.getName();
                String direccion=f.getAbsolutePath();
                long tamanio=f.length();                
                dos = new DataOutputStream(cl.getOutputStream());
                dis = new DataInputStream(new FileInputStream(direccion));
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
                System.out.println("\nArchivo enviado");
                dis.close();
                dos.close();
                cl.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Descargar(){
        try {
            cl=new Socket(direccion,puerto);
            dis=new DataInputStream(cl.getInputStream());
            String nombre=dis.readUTF();
            long tamanio=dis.readLong();
            String aux=new File("").getAbsolutePath()+"/Descargas/"+nombre;
            dos=new DataOutputStream(new FileOutputStream(aux));
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
            System.out.println("\nArchivo recibido");
            dis.close();
            dos.close();
            cl.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void Eliminar(){
        try {
            cl=new Socket(direccion,puerto);
            dis=new DataInputStream(cl.getInputStream());
            if(dis.readBoolean())
                System.out.println("Eliminado exitosamente");
            else
                System.out.println("No se pudo eliminar");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Abrir(String direccion_relativa) {
        try {
            cl=new Socket(direccion,puerto);
            dos=new DataOutputStream(cl.getOutputStream());
            dis=new DataInputStream(cl.getInputStream());
            System.out.println("---------------------------------------------------------------");
            System.out.println("Mostrando contenido en "+direccion_relativa);
            int num_direcr=dis.readInt();
            String nombre;
            for(int i=0;i!=num_direcr;i++){
                nombre=dis.readUTF();  
                System.out.println(nombre);    
            }
            System.out.println("---------------------------------------------------------------");
    
            cl.close();
            dos.close();
            dis.close();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        new Cliente().Menu();
    }//main

    public void Menu() {
        //Vector <String> Directorio=SolicitarDirectorio("");
        descargas="./Descargas";
        String comando;
        while (true) {
            System.out.println("Modo de uso:");
            System.out.println("Acciones: d:descargar;e:eliminar;a:abrir;s:subir;c:cerrar conexion");
            System.out.println("Ejemplos:");
            System.out.println("\td ./reporte.pdf");
            System.out.println("\ta ./Ejercicios/carpeta tareas");
            System.out.println("\te system32");
            System.out.println("\ts ./Capeta destino");
            System.out.println("Modo de uso:");
            System.out.println("Directorio del servidor:\n__________________________");
            Procesar("a ./");
            System.out.println("__________________________\nIngrese comando:");
            comando=sc.nextLine();
            if(Procesar(comando))
                System.out.println("Comando desconocido,pruebe de nuevo");
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


/*public void Conectar(){
        //User input
        System.out.println("Ingresa el IP del servidor");
        //dir = sc.nextLine();
        direccion="127.0.0.1";//Esto se puede retirar, 
        puerto = 8000;
        try{
            System.out.println("Intentando conectar servidor en el puerto "+pto);
            cl = new Socket(dir,pto);
            System.out.println("Conexion con servidor establecida...");
            Menu();
        }catch(Exception e){
            System.out.println("Conexion con servidor fallida...");
            e.printStackTrace();
        }//catch
    }*/
    