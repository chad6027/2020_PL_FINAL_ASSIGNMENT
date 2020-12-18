package aroundearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server implements Runnable{
	
	public int port;
	private final int max_client_num = 10;
	private  static Socket client_sock;
	private  static HashMap<String, Socket> client_socks = new HashMap<String, Socket>();
	private  static HashMap<Socket, PrintWriter> client_outBuffers = new HashMap<Socket, PrintWriter>();
	public Server(int port) {
		this.port = port;
	}
	// client�� �޾� hashmap<String, Socket>�� �߰�
	public void waitForClient( ExecutorService eService) {
		try (ServerSocket sSocket = new ServerSocket(port)){
			
			System.out.println("���� ��� �� ......");
			client_sock = sSocket.accept();
			System.out.println("Connected list");
		
			
			ChatThread ct = new ChatThread(client_sock);
			eService.execute(ct);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addClient(String nickname, Socket clnt_sock) {
		System.out.println(nickname + "���� �����ϼ̽��ϴ�.");
		client_socks.put(nickname, clnt_sock);
	}
	
	public void removeClient(String nickname) {
		System.out.println(nickname + "���� �����ϼ̽��ϴ�.");
		client_socks.remove(nickname);
	}
	
	public void addOutBuffer(Socket clnt_sock, PrintWriter out) {
		client_outBuffers.put(clnt_sock, out);
	}
	
	public int getVoting() {
		
		return 0;
	}
	
	public void setSkrull() {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		ExecutorService client_thread_pool = Executors.newFixedThreadPool(max_client_num);
		
		for(int i = 0 ; i < max_client_num; ++i) {
			waitForClient(client_thread_pool); 
			
		}
		client_socks.forEach((nick, clnt) ->{ System.out.println("nickname : " + nick + "\tcclient_sock : " + clnt);});
		setSkrull();
		
		
		while(true) {

			// Client���� ä�� �ð� �ο�
			
			getVoting();
			
			// Game ���� Ȯ��
			
		}
	}
	
	public synchronized void sendMsg(String msg) {
		
		client_outBuffers.forEach((clnt, out)->{ out.println(msg); System.out.println("Send msg to " + clnt);});
		
	}
	
	//-------------------------------------------
	class ChatThread implements Runnable{
		private Socket clnt_sock;
		private BufferedReader br;
		private PrintWriter out;
		private String nickname;
		
		
		public ChatThread(Socket client_sock) throws IOException {
			clnt_sock = client_sock;
			br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
			out = new PrintWriter(client_sock.getOutputStream(), true);
			nickname = br.readLine();
			
			System.out.println("ChatThread");
			
			addClient(nickname, client_sock);
			addOutBuffer(client_sock, out);
			
			sendMsg(nickname + " has entered the chat");
		}

		@Override
		// ������� ���⼭ ó��
		// ������ ��� �����ֱ⸸ �ϸ� �ȴ�. 
		public void run() {
			System.out.println("ChatThread Run");
			// TODO Auto-generated method stub
			String client_msg;
			try {
				
				while( (client_msg = br.readLine()) != null){
					System.out.println(client_msg);
					sendMsg(nickname + " : " + client_msg);
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				removeClient(nickname);
			}
		
	
		}
	}
}

