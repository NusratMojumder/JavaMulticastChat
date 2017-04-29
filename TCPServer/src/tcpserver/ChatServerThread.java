
package tcpserver;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class ChatServerThread extends Thread
{  private TCPServer       server    = null;
   private Socket           socket    = null;
   private int              ID        = -1;
   private DataInputStream  streamIn  =  null;
   private DataOutputStream streamOut = null;
   private int login_flag = 0;
   private int friend_req_send_flag = 0;
   private int friend_req_recieve_flag = 0;
   private int current_req = 0;
   ArrayList<String> friend_list = new ArrayList<String>();
   ArrayList<String> offline_msg= new ArrayList<String>();
   ArrayList<String> history= new ArrayList<String>();

   public ChatServerThread(TCPServer _server, Socket _socket)
   {  super();
      server = _server;
      socket = _socket;
      ID     = socket.getPort();
      
   }
   public void send(String msg)
   {   try
       {  streamOut.writeUTF(msg);
          streamOut.flush();
       }
       catch(IOException ioe)
       {  System.out.println(ID + " ERROR sending: " + ioe.getMessage());
          server.remove(ID);
          stop();
       }
   }
   
   public synchronized void set_login_flag(int flag) 
   {
        login_flag = flag;
   }
   public synchronized int return_login_flag() 
   {
	return login_flag;
   }
   
   public synchronized void set_friend_req_send(int flag) 
   {
       friend_req_send_flag=flag;
   }
   public synchronized int return_friend_req_send() {
      return friend_req_send_flag;
   }
   
    public synchronized void set_current_req(int ID) 
   {
       current_req=ID;
   }
   public synchronized int return_current_req() {
      return current_req;
   }
   
    public synchronized void set_friend_req_recieve(int flag) 
   {
       friend_req_recieve_flag=flag;
   }
   public synchronized int return_friend_req_recieve() {
      return friend_req_recieve_flag;
   }
   
   public synchronized void add_friend_list(String ID) 
   {
        friend_list.add(ID);
    }
    public synchronized String return_friend_list() 
    {
	return friend_list.toString();
    }
    public synchronized int check_friend_list(String ID) 
    {
	int flag;      

        if (friend_list.contains(ID.trim())){
            flag=1;
        }
        else{
            flag=0;
        }
        
    return flag;   
    }
   
    public synchronized void add_offline_msg(String input) 
    {
	offline_msg.add(input);
    }
    public synchronized String return_offline_msg() 
    {	
	return offline_msg.toString();
    }
    public synchronized void add_history(String input) 
    {
	history.add(input);
    }
    public synchronized String return_history() 
    {	
	return history.toString();
    }
    
   public int getID()
   {  return ID;
   }
   
   public void run()
   {  System.out.println("Server Thread " + ID + " running.");
      while (true)
      {  try
         {  server.handle(ID, streamIn.readUTF());
         }
         catch(IOException ioe)
         {  System.out.println(ID + " ERROR reading: " + ioe.getMessage());
            server.remove(ID);
            stop();
         }
      }
   }
   public void open() throws IOException
   {  streamIn = new DataInputStream(new 
                        BufferedInputStream(socket.getInputStream()));
      streamOut = new DataOutputStream(new
                        BufferedOutputStream(socket.getOutputStream()));
   }
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (streamIn != null)  streamIn.close();
      if (streamOut != null) streamOut.close();
   }
}