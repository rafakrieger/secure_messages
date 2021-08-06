/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.security.dao;

import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import br.com.security.entidade.Message;

public interface MessageDao extends BaseDao<Message, Long> {

    List<Message> searchMessages(Long receiver, Session sessao) throws HibernateException;    
}
