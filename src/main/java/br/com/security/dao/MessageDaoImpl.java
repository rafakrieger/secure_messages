package br.com.security.dao;

import java.io.Serializable;
import java.util.List;
import org.hibernate.HibernateException;

import br.com.security.entidade.Message;

import org.hibernate.*;

public class MessageDaoImpl extends BaseDaoImpl<Message, Long> implements MessageDao, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public Message pesquisarPorId(Long id, Session sessao) throws HibernateException {
		return (Message) sessao.get(Message.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Message> searchMessages(Long receiver, Session sessao) throws HibernateException {
		Query consulta = sessao.createQuery("from Message where receiver = :receiver");
        consulta.setParameter("receiver", "receiver");
        return consulta.list();
	}

	
}
