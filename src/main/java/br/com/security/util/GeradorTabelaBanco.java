package br.com.security.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;



public class GeradorTabelaBanco {
    
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("secret_messages");
        emf.close();
    }
}
