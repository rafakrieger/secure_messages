/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.security.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import br.com.security.entidade.User;

public interface UserDao extends BaseDao<User, Long> {
	
    List<User> pesquisarTodos(Session sessao) throws HibernateException;
    User pesquisarPorLogin(String login, Session sessao) throws HibernateException;
    User login(String login, String password, Session sessao) throws HibernateException;
    String getPublicKey(Long id, Session sessao) throws HibernateException;
    byte[] getPrivateKey(Long id, Session sessao) throws HibernateException;
}
