package tcpserver;

import java.io.*;
import java.util.*;

public class userpassword_match {

 private static final String[] username = { "sraboni", "fariha", "maisha", "rafsan"};
 private static final String[] password = { "1234", "abcd", "111111", "abc123"}; 
    
    public static int SearchList(String client_username, String client_passwd) {
       int flag = 0;      
             
             for(int i = 0; i < username.length; i++)
     {
          if (username[i].equals(client_username))
          {
                if (password[i].equals(client_passwd))
                {
                     flag = 1;
                     break;
                }
          }
     }
             
             return flag;
}
    
}
