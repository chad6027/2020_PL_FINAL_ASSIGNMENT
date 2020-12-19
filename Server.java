package aroundearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server implements Runnable{
	
	public int port;
	private int skrull_number;
	private final int max_client_num = 3;
	private  static Socket client_sock;
	private  static ServerSocket sSocket;
	
	private static Vector<Socket> client_socks_v= new Vector<Socket>(); 
	private  static HashMap<String, Socket> client_socks = new HashMap<String, Socket>();
	private  static HashMap<Socket, PrintWriter> client_outBuffers = new HashMap<Socket, PrintWriter>();
	
	public Server(int port) throws IOException {
		this.port = port;
		sSocket = new ServerSocket(port);
	}
	
	// client를 받아 hashmap<String, Socket>에 추가
	public void waitForClient( ExecutorService eService) {
		
			try {
				client_sock = sSocket.accept();
				ChatThread ct = new ChatThread(client_sock);
				eService.execute(ct);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
	}
	
	public void addClient(String nickname, Socket clnt_sock) {
		client_socks.put(nickname, clnt_sock);
		client_socks_v.add(clnt_sock);
		sendMsg(clnt_sock, nickname + "님이 입장하셨습니다.");
	}
	
	public void removeClient(String nickname) {
		client_socks.remove(nickname);
		broadcast(nickname + "님이 퇴장하셨습니다.");
		
	}
	
	public void addOutBuffer(Socket clnt_sock, PrintWriter out) {
		client_outBuffers.put(clnt_sock, out);
	}
	
	public int getVoting() {
		
		return 0;
	}
	
	public void setSkrull() {
		broadcast("Skrull을 뽑는 중입니다.");
		Random rand = new Random();
		skrull_number = rand.nextInt(max_client_num);
		
		
		for(int i = 0; i < max_client_num; i++) {
			if(i == skrull_number) 
				client_outBuffers.get(client_socks_v.get(i)).println("Skrull");
			else
				client_outBuffers.get(client_socks_v.get(i)).println("Human");
		}
		
		
	}
	
	
	public static synchronized void sendMsg(Socket sock, String msg) {
		Socket clnt_from = sock;
		client_outBuffers.forEach((clnt, out)->{ if(clnt != clnt_from) out.println(msg);});
	}

	public static synchronized void broadcast(String msg) {
		client_outBuffers.forEach((clnt, out)->{out.println(msg);});
	}

	
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		ExecutorService client_thread_pool = Executors.newFixedThreadPool(max_client_num);
		for(int i = 0 ; i < max_client_num; ++i) {
			waitForClient(client_thread_pool); 
			System.out.println((max_client_num - 1 - i ) + "명 남았습니다.");
		}
		
		System.out.println("Game Start");
		
		setSkrull();
		
		
		while(true) {

			// Client에게 채팅 시간 부여
			
			getVoting();
			
			// Game 종료 확인
			
		}
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
			
			addClient(nickname, client_sock);
			addOutBuffer(client_sock, out);
			
			sendMsg(clnt_sock, nickname + " has entered the chat");
		}

		@Override
		// 입출력을 여기서 처리
		// 서버는 듣고 보내주기만 하면 된다. 
		public void run() {
			// TODO Auto-generated method stub
			String client_msg;
			try {
				
				while( (client_msg = br.readLine()) != null){
					sendMsg(clnt_sock, nickname + " : " + client_msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				removeClient(nickname);
			}
		}
	}
}

