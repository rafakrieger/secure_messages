package br.com.security.controle;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import br.com.security.dao.HibernateUtil;
import br.com.security.dao.MessageDao;
import br.com.security.dao.MessageDaoImpl;
import br.com.security.dao.UserDao;
import br.com.security.dao.UserDaoImpl;
import br.com.security.entidade.Message;
import br.com.security.entidade.User;
import br.com.security.util.UtilGerador;

@ManagedBean(name = "userC")
@ViewScoped
public class UserControl implements Serializable {

	private static final long serialVersionUID = 1L;

	private User user;
	private UserDao userDao;
	private Session sessao;
	private List<User> users;
	private List<SelectItem> comboUsers;
	private PrivateKey privateKey;
	private byte[] pk;
	private byte[] pkRead;
	private List<Message> messages;
	private Message message;
	private MessageDao messageDao;
	private PublicKey publicKey;
	private String pkPath;
	private String content;
	public static Long idLogin;
	public static String pkPassword = UtilGerador.gerarCaracter(16);
	public static final String ALGORITHM = "RSA";
	public static final String PATH_PUBLIC_KEY = "C:/keys/public.key";
	public static final String PATH_PRIVATE_KEY = "C:/keys/private.key";

	public UserControl() {
		userDao = new UserDaoImpl();
		messageDao = new MessageDaoImpl();		
	}

	public static void generateKey() {
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			final KeyPair keys = keyGen.generateKeyPair();

			File privateKeyFile = new File(PATH_PRIVATE_KEY);
			File publickeyFile = new File(PATH_PUBLIC_KEY);

			// create the directory to store the keys files when it does not exists
			if (privateKeyFile.getParentFile() != null) {
				privateKeyFile.getParentFile().mkdirs();
			}

			privateKeyFile.createNewFile();

			if (publickeyFile.getParentFile() != null) {
				publickeyFile.getParentFile().mkdirs();
			}

			publickeyFile.createNewFile();

			// Save public key content into a File
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publickeyFile));
			publicKeyOS.writeObject(keys.getPublic());
			publicKeyOS.close();

			// Save private key content into a File
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(keys.getPrivate());
			privateKeyOS.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void encryptPrivateKey() {
		ObjectInputStream inputStream = null;
		try {
			inputStream = new ObjectInputStream(new FileInputStream(PATH_PRIVATE_KEY));
			privateKey = (PrivateKey) inputStream.readObject();
			try {
				pk = AES.encrypt(privateKey.getEncoded(), pkPassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
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
	
	// SALVAR USUÁRIO
	
	public void save() {
		sessao = HibernateUtil.abrirSessao();
		if (userDao.pesquisarPorLogin(user.getLogin(), sessao) == null) {
			try {
				generateKey();
				encryptPrivateKey();
				user.setPrivateKey(pk);
				user.setPublicKeyPath(PATH_PUBLIC_KEY);
				user.setPassword(hexaToString(sha256(user.getPassword())));
				userDao.salvarOuAlterar(user, sessao);
				user = null;
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Salvo com sucesso", null));
			} catch (HibernateException e) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao salvar", null));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} finally {
				sessao.close();
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login já existe", null));
		}
	}
	
	
	public void delete() {
		sessao = HibernateUtil.abrirSessao();
		try {
			userDao.excluir(user, sessao);
			user = null;
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Excluído com sucesso", null));
		} catch (HibernateException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir", null));
		} finally {
			sessao.close();
		}
	}

	//CARREGAR COMBO BOX DE USUÁRIOS
	
	public void loadUsers() {
		sessao = HibernateUtil.abrirSessao();
		try {
			users = userDao.pesquisarTodos(sessao);
			comboUsers = new ArrayList<>();
			for (User u : users) {
				comboUsers.add(new SelectItem(u.getId(), u.getNome()));
			}
		} catch (HibernateException e) {
			System.out.println("Erro ao carregar combo box: " + e.getMessage());
		} finally {
			sessao.close();
		}
	}
	
	//VALIDAR LOGIN

	public String login() {
		sessao = HibernateUtil.abrirSessao();
		user = userDao.login(user.getLogin(), user.getPassword(), sessao);
		if (user == null) {
			user = new User();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Authentication error", null));
			sessao.close();
			return null;
		} else {
			idLogin = user.getId();
			sessao.close();
			return "/principal";
		}
	}

	public String logout() {
		user = null;
		return "/login";
	}
	
	// ENVIAR MENSAGEM

	public void send() {
		sessao = HibernateUtil.abrirSessao();
		user = userDao.pesquisarPorId(idLogin, sessao);
		pkPath = userDao.getPublicKey(message.getReceiver().getId(), sessao);
		sessao.close();
		ObjectInputStream inputStream = null;
		if (pkPath != null) {			
			try {
				sessao = HibernateUtil.abrirSessao();
				message.setContent(content.getBytes("UTF-8"));
				inputStream = new ObjectInputStream(new FileInputStream(pkPath));
				publicKey = (PublicKey) inputStream.readObject();
				byte[] messageCyphered = RSA.encrypt(message.getContent(), publicKey);
				File f = new File("message_cyphered");
				FileOutputStream fos = new FileOutputStream(f);
				DataOutputStream dos = new DataOutputStream(fos);
				dos.write(messageCyphered);
				dos.close();
				fos.close();

				// store in database
				message.setContent(messageCyphered);
				message.setSender(user);
				messageDao.salvarOuAlterar(message, sessao);
				message = null;
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Enviado com sucesso", null));

			} catch (ClassNotFoundException ex) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao enviar", null));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				sessao.close();
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Chave pública não encontrada", null));
		}
	}
	
	public boolean verifyPrivateKey() {
		boolean verified = true;
		sessao = HibernateUtil.abrirSessao();
		ObjectInputStream inputStream = null;
		try {
			pk = AES.decrypt(userDao.getPrivateKey(idLogin, sessao), pkPassword);
			
			inputStream = new ObjectInputStream(new FileInputStream(PATH_PRIVATE_KEY));
			pkRead = (byte[]) inputStream.readObject();
			
			if (!pk.equals(pkRead)) {
				verified = false;
			}
			
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid private key", null));
		} finally {
			sessao.close();
		}
		return verified;
	}

	public void decryptMessage() {
		sessao = HibernateUtil.abrirSessao();
		try {
			if (verifyPrivateKey()) {
				messages = messageDao.searchMessages(idLogin, sessao);
				for (int i = 0; i < messages.size(); i++) {				
					byte[] messageDecrypted = RSA.decrypt(messages.get(i).getContent(), privateKey);							
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, messageDecrypted.toString(), null));
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Decrypt error", null));
		}
	}


	// GET SET

	public User getUser() {
		if (user == null) {
			user = new User();
		}
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<SelectItem> getComboUsers() {
		return comboUsers;
	}

	public Message getMessage() {
		if (message == null) {
			message = new Message();
		}
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public String getPkPath() {
		return pkPath;
	}

	public void setPkPath(String pkPath) {
		this.pkPath = pkPath;
	}

	public String getContent() {
		if (content == null) {
			content = new String();
		}
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}


}
