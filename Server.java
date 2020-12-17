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
	private final int max_client_num = 3;
	private  static Socket client_sock;
	private  static HashMap<String, Socket> client_socks = new HashMap<String, Socket>();
	private  static HashMap<Socket, PrintWriter> client_outBuffers = new HashMap<Socket, PrintWriter>();
	public Server(int port) {
		this.port = port;
	}
	// client를 받아 hashmap<String, Socket>에 추가
	public void waitForClient( ExecutorService eService) {
		try (ServerSocket sSocket = new ServerSocket(port)){
			
			System.out.println("연결 대기 중 ......");
			client_sock = sSocket.accept();
			
			ChatThread ct = new ChatThread(client_sock);
			eService.execute(ct);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addClient(String nickname, Socket clnt_sock) {
		System.out.println(nickname + "님이 입장하셨습니다.");
		client_socks.put(nickname, clnt_sock);
	}
	
	public void removeClient(String nickname) {
		System.out.println(nickname + "님이 퇴장하셨습니다.");
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

		ExecutorService client_thread_pool = Executors.newFixedThreadPool(4);
		
		for(int i = 0 ; i < max_client_num; ++i) {
			waitForClient(client_thread_pool); 
		}
		
		setSkrull();
		
		
		while(true) {

			// Client에게 채팅 시간 부여
			
			getVoting();
			
			// Game 종료 확인
			
		}
	}
	
	public synchronized void sendMsg(String msg) {
		
		client_outBuffers.forEach((clnt, out)->{ out.println(msg);});
		
	}
	
	//-------------------------------------------
	class ChatThread implements Runnable{
		private BufferedReader br;
		private PrintWriter out;
		private String nickname;
		
		
		public ChatThread(Socket client_sock) throws IOException {
			br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
			out = new PrintWriter(client_sock.getOutputStream(), true);
			nickname = br.readLine();
			
			addClient(nickname, client_sock);
			addOutBuffer(client_sock, out);
		}

		@Override
		// 입출력을 여기서 처리
		// 서버는 듣고 보내주기만 하면 된다. 
		public void run() {
			// TODO Auto-generated method stub
			String client_msg;
			try {
				while( (client_msg = br.readLine()) != null){
					sendMsg(nickname + " : " + client_msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				removeClient(nickname);
			}
		
	
		}
	}
}

