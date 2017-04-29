package tcpserver;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TCPServer implements Runnable
{  private ChatServerThread clients[] = new ChatServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private int login_match = 0;
   private int user_ = 0;
   private int passw_ = 0;
   private String username="";
   private String password="";
   private int unicast_flag = 0;
   private int unicast_id = 0;
   private int multicast_flag = 0;
   private String server_remove_port;

   private String show_friend_list="No Friends Yet";
   ArrayList<Integer> multicast_id = new ArrayList<Integer>();
   ArrayList<String> hist= new ArrayList<String>();


   public TCPServer (int port)
   {  try
      {  System.out.println("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);  
         System.out.println("Server started: " + server);
         start(); }
      catch(IOException ioe)
      {  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); }
   }
@Override
   public void run()
   {  while (thread != null)
      {  try
         {  System.out.println("Waiting for a client ..."); 
            addThread(server.accept()); }
         catch(IOException ioe)
         {  System.out.println("Server accept error: " + ioe); stop(); }
      }
   }
   public void start()  
   { 
       if (thread == null)
      {  thread = new Thread(this); 
         thread.start();
      }
        
       BufferedReader inFromServer=new BufferedReader(new InputStreamReader(System.in));
       
       while(true){
        try {
            server_remove_port=inFromServer.readLine();

        } catch (IOException ex) {}

            if (server_remove_port.contains("remove:") && server_remove_port.indexOf("remove:") == 0) {

            int index = server_remove_port.indexOf(":") +1;
            String matchPort = server_remove_port.substring(index,server_remove_port.trim().length());
            System.out.println(matchPort);

            if(Pattern.matches("[0-9]+", matchPort) == true){

                    for (int i = 0; i < clientCount; i++){
                        if (clients[i].getID() == Integer.parseInt(matchPort))
                        { 
                              System.out.println("Port number: "+matchPort);
                              clients[findClient(Integer.parseInt(matchPort))].send(".bye");
                              remove(Integer.parseInt(matchPort));
                        }
                    }
                }
            else {
                System.out.println("Invalid port");
                }
            }
            else {
            System.out.println("Invalid input. Format : remove:portnumber");
            }
        }
   }
   
   public void stop()
   { 
   if (thread != null)
      {  thread.stop(); 
         thread = null;
      }
   }
   
   private int findClient(int ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }
   
   private String matchClient(String ID) 
   {  for (int i = 0; i < clientCount; i++)
        if (clients[i].getID() == Integer.parseInt(ID) && clients[i].return_login_flag()==1)
        { 
            return ID;
        }
      return null;
	
   }
   
   
   public synchronized void handle(int ID, String input)
   {          
        //////////////////////////////% LEAVING PROGRAM %///////////////////////////////
        
       if (input.equals(".bye")) {
            clients[findClient(ID)].send(".bye");
            remove(ID); 
        }

        
        //////////////////////////////% USER LOG IN %/////////////////////////////////
        
	if (input.equals("login")) {
            clients[findClient(ID)].send("Enter Username: ");
            user_=1;
        }
        
        //Getting Username
        else if (user_== 1) {
            clients[findClient(ID)].send("Enter Password: ");
            username=input;
            passw_=1;
            user_=0;
        }
        
        //Getting Password and matching
        else if (passw_== 1) {
            password=input;
            passw_= 0;
            user_= 0;
            login_match = userpassword_match.SearchList(username,password);
            
            //Login check, match username and password
            if(login_match==1){
                    clients[findClient(ID)].send("Login Successfull\n"
                            + "Welcome to 'public' chat\n"
                            + "'add friend' for 'private message' , 'group message'  and 'private chat'");
                    clients[findClient(ID)].set_login_flag(1);
                     
                    for (int i = 0; i < clientCount; i++){
                     if(clients[i].return_login_flag()==1)
                     clients[i].send(ID + ": " +clients[findClient(ID)].return_offline_msg() ); 
                }
            }
            //Login failed
            else{
                    clients[findClient(ID)].send("Login failed"); 
                    clients[findClient(ID)].set_login_flag(0);
            }
        }
        
        //Logging out
        else if (input.equals("logout")) {
            clients[findClient(ID)].send("Logging Out");
            clients[findClient(ID)].set_login_flag(0);
        }
        
        
        ///////////////////% MESSAGE SENDING %/////////////////////////////////
        
        else if(clients[findClient(ID)].return_login_flag()==1){
            
            ////////////////////////////////////HISTORY////////////////////////////////////////
            
            //Adding History
            if(!(input.equals("history")) && !(input.equals("my history"))){
                  hist.add(ID + ": " + input);
                  clients[findClient(ID)].add_history(input); 
            }
            
            //Showing Personal History
            if(input.equals("my history")){
                  clients[findClient(ID)].send(clients[findClient(ID)].return_history());  
            }
            
            //Showing Total History
            else if(input.equals("history")){
                  System.out.println("History: " + hist);
                  clients[findClient(ID)].send(hist+"");  
            }
            
            
            ////////////////////////////////////FRIEND REQUEST////////////////////////////////////////
           
            else if(input.equals("add friend") && clients[findClient(ID)].return_friend_req_send()==0){
                
                  clients[findClient(ID)].send("Who do you want to add?");
                  
                  //Show list of available users not in freind list
                     for (int i = 0; i < clientCount; i++){
                        if(clients[i].getID()!=ID && clients[i].return_login_flag()==1 
                                && clients[findClient(ID)].check_friend_list(Integer.toString(clients[i].getID()))==0) 
                            
                        { 
                            clients[findClient(ID)].send("Client"+i+" : "+clients[i].getID());
                        }
                     }
                    clients[findClient(ID)].set_friend_req_send(1);
                    
           }
            
           //Seding friend request
            else if(clients[findClient(ID)].return_friend_req_send()==1){
                
            int friend_req;
            
            if(Pattern.matches("[0-9]+", input) == true){
                //Send request to valid user not in friend list
                if (input.equals(matchClient(input)) && clients[findClient(ID)].check_friend_list(input)==0){
                        
                        friend_req = Integer.parseInt(input);
                        clients[findClient(ID)].send("Friend Request sent to "+friend_req);
                        clients[findClient(friend_req)].send("You have a friend request from "+ID
                                                    + "\nEnter 'yes' to accept, 'no' to decline");

                        clients[findClient(friend_req)].set_friend_req_recieve(1);
                        clients[findClient(friend_req)].set_current_req(ID);

                        clients[findClient(ID)].set_friend_req_send(0); 

                    }
                else{
                        clients[findClient(ID)].send("Invalid port number, try again");
                    } 
            }
            else{
                clients[findClient(ID)].send("Input integer port number");
            } 
                   
           }
            
            //Recieving friend request
            else if(clients[findClient(ID)].return_friend_req_recieve()==1){
                
                int add_friend=clients[findClient(ID)].return_current_req();
                
                //Accepting friend
                if (input.equals("yes")){
                    clients[findClient(add_friend)].send(ID+" accepted your friend request");
                    clients[findClient(ID)].send("You accepted a request from "+add_friend);
                    
                    //Adding to friend list
                    clients[findClient(add_friend)].add_friend_list(Integer.toString(ID));;
                    clients[findClient(ID)].add_friend_list(Integer.toString(add_friend));
                    
                    clients[findClient(ID)].set_friend_req_recieve(0);
                    clients[findClient(ID)].set_current_req(0);
                    
                }
                //Rejecting friend
                else if (input.equals("no")){
                    clients[findClient(add_friend)].send(ID+" rejected your friend request");
                    clients[findClient(ID)].send("You rejected a request from "+add_friend);
                     clients[findClient(ID)].set_friend_req_recieve(0);
                    clients[findClient(ID)].set_current_req(0);
                }
                else{
                    clients[findClient(ID)].send("Invalid choice, try again");
                }
           }
           else if (input.equals("friend list")){
                show_friend_list = clients[findClient(ID)].return_friend_list();
		clients[findClient(ID)].send(show_friend_list);
                show_friend_list="";
           }
           
           ////////////////////////////////////UNICAST//////////////////////////////////////////
           
           else if (input.equals("private message")){
               
               clients[findClient(ID)].send("Here are available friends\n Whom do you want to send a private message to?");
               
               //Find users from friend list who are logged in
                for (int i = 0; i < clientCount; i++){
                        if(clients[i].getID()!=ID && clients[i].return_login_flag()==1 
                                && clients[findClient(ID)].check_friend_list(Integer.toString(clients[i].getID()))==1) 
                          
                            clients[findClient(ID)].send("Client"+i+" : "+clients[i].getID());
                        
                }
                unicast_flag=1;
           }
           
           //Check if given port is available and valid
           else if (unicast_flag==1){
               if(Pattern.matches("[0-9]+", input) == true){    
                    if (input.equals(matchClient(input)) && clients[findClient(ID)].check_friend_list(input)==1 
                            && clients[findClient(Integer.parseInt(input))].return_login_flag()==1 ){
                        clients[findClient(ID)].send("Send your private message to "+input);

                        unicast_flag=2;
                        unicast_id=Integer.parseInt(input);
                    }
                    else
                    {
                        clients[findClient(ID)].send("Invalid user,try again");
                    }
                }
               else
               {
                        clients[findClient(ID)].send("invalid, Enter Integer user ID");
                } 
               
            }
           
            //Exit private message
            else if (unicast_flag==2 && input.equals("exit private")){
               clients[findClient(ID)].send("You have left private message");
               unicast_flag=0;
               unicast_id=0;
            }
           
           //Check if given port is available and valid
           else if (unicast_flag==2 && !(input.equals("exit private")))
           {
               clients[findClient(unicast_id)].send(ID+" : "+input);
               clients[findClient(ID)].send("Your message was sent to "+unicast_id);   
            }
           
           
           ////////////////////////////////////MULTICAST//////////////////////////////////////////
           
           else if (input.equals("group message")){
               
               clients[findClient(ID)].send("Here are available friends\n Which people do you want to private message?");
               
               //Find users from friend list who are logged in
                for (int i = 0; i < clientCount; i++){
                        if(clients[i].getID()!=ID && clients[i].return_login_flag()==1 
                                && clients[findClient(ID)].check_friend_list(Integer.toString(clients[i].getID()))==1) 
                          
                            clients[findClient(ID)].send("Client"+i+" : "+clients[i].getID());
                        
                }
                clients[findClient(ID)].send("Write done after entering desired user id's");
                multicast_flag=1;
           }
           
            //Stop adding users
            else if (multicast_flag==1 && input.trim().equals("done")){
                 clients[findClient(ID)].send("Got your group for private message, please write your message now");
                 clients[findClient(ID)].send(multicast_id+""); 
                 multicast_flag=2;
            }
      
            
           //Check if given port is available and valid and add port to group
           else if (multicast_flag==1){
               if(Pattern.matches("[0-9]+", input) == true){    
                    if (input.equals(matchClient(input)) && clients[findClient(ID)].check_friend_list(input)==1 
                            && clients[findClient(Integer.parseInt(input))].return_login_flag()==1 ){
                 
                        multicast_id.add(Integer.parseInt(input));
                        clients[findClient(ID)].send(input+" added");
                    }
                    else
                    {
                        clients[findClient(ID)].send("Invalid user,try again");
                    }
                }
               else
               {
                        clients[findClient(ID)].send("invalid, Enter Integer user ID");
                } 
               
            }
            
            //Exit group message
            else if (multicast_flag==2 && input.equals("exit group")){
               clients[findClient(ID)].send("You have left group message");
               multicast_flag=0;
               multicast_id=null;
               System.out.print(multicast_id);
            }
           
           //Check if given port is available and valid
           else if (multicast_flag==2 && !(input.equals("exit group")))
           {
               for (int i : multicast_id) 
               {
                    clients[findClient(i)].send(ID + "sent:" + input);
		}  
            }
           
                      
          ////////////////////////////////////BROADCAST////////////////////////////////////////
           else{
            //Broadcast
                for (int i = 0; i < clientCount; i++){
                     if(clients[i].return_login_flag()==1)
                     {
                     clients[i].send(ID + ": " + input); 
                     }
                }
           }
        }
        
        else
           clients[findClient(ID)].send("You are not logged in");
         if(!(input.equals("login")) && !(input.equals(username)) && !(input.equals(password))){
                clients[findClient(ID)].add_offline_msg(input);
            }
        
   }
   public synchronized void remove(int ID)
   {  int pos = findClient(ID);
      if (pos >= 0)
      {  ChatServerThread toTerminate = clients[pos];
         System.out.println("Removing client thread " + ID + " at " + pos);
         if (pos < clientCount-1)
            for (int i = pos+1; i < clientCount; i++)
               clients[i-1] = clients[i];
         clientCount--;
         try
         {  toTerminate.close(); }
         catch(IOException ioe)
         {  System.out.println("Error closing thread: " + ioe); }
         toTerminate.stop(); }
   }
   private void addThread(Socket socket)
   {  if (clientCount < clients.length)
      {  System.out.println("Client accepted: " + socket);
         clients[clientCount] = new ChatServerThread(this, socket);
         try
         {  clients[clientCount].open(); 
            clients[clientCount].start();  
            clientCount++; }
         catch(IOException ioe)
         {  System.out.println("Error opening thread: " + ioe); } }
      else
         System.out.println("Client refused: maximum " + clients.length + " reached.");
   }
   public static void main(String args[]) { TCPServer server = null;
         server = new TCPServer(2000); }
}