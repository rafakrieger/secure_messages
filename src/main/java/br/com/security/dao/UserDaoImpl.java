package br.com.security.dao;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.HibernateException;
import org.hibernate.*;
import org.hibernate.Session;

import br.com.security.entidade.User;

public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public User pesquisarPorId(Long id, Session sessao) throws HibernateException {
		return (User) sessao.get(User.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> pesquisarTodos(Session sessao) throws HibernateException {
		return sessao.createQuery("from User order by nome").list();
	}

	@Override
	public User pesquisarPorLogin(String login, Session sessao) throws HibernateException {
		Query consulta = sessao.createQuery("from User where login = :login");
		consulta.setParameter("login", login);
		return (User) consulta.uniqueResult();
	}

	@Override
	public User login(String login, String password, Session sessao) throws HibernateException {
		try {
			password = hexaToString(sha256(password));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		try {
			Query consulta = sessao.createQuery("from User where login = :login and password = :password");
			consulta.setParameter("login", login);
			consulta.setParameter("password", password);
			return (User) consulta.uniqueResult();
		} catch (NoResultException e) {
			return null;
		}

	}
	
	@Override
	public String getPublicKey(Long id, Session sessao) throws HibernateException {
		Query consulta = sessao.createQuery("select publicKeyPath from User where id = :id");
		consulta.setParameter("id", id);
		return (String) consulta.uniqueResult();
	}
	
	
	public static byte[] sha256(String message) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256"); // SHA (Secure Hash Algorithm)
		md.update(message.getBytes());
		byte[] digest = md.digest();
		return digest;
	}

	public static String hexaToString(byte[] digest) {
		// Convert digest to a string
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			if ((0xff & digest[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & digest[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & digest[i]));
			}
		}
		return hexString.toString();
	}

	@Override
	public byte[] getPrivateKey(Long id, Session sessao) throws HibernateException {
		Query consulta = sessao.createQuery("select privateKey from User where id = :id");
		consulta.setParameter("id", id);
		return (byte[]) consulta.uniqueResult();
	}

}
