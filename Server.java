package aroundearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server implements Runnable{
	
	public int port;
	private int day;
	private int skrull_number;
	private final int max_client_num = 3;
	private boolean VOTE_MODE = false;
	private ExecutorService client_thread_pool = Executors.newFixedThreadPool(max_client_num);
	private static Vector<ChatThread> threads = new Vector<ChatThread>();
	
	private  static Socket client_sock;
	private  static ServerSocket sSocket;

	
	private static Vector<Pair<Socket, Boolean>> client_socks_v= new Vector<Pair<Socket, Boolean>>(); 
	private  static HashMap<String, Socket> client_socks = new HashMap<String, Socket>();
	private  static HashMap<Socket, Pair<BufferedReader, Boolean>> client_inBuffers = new HashMap<Socket, Pair<BufferedReader, Boolean>>();
	private  static HashMap<Socket, PrintWriter> client_outBuffers = new HashMap<Socket, PrintWriter>();
	
	public Server(int port) throws IOException {
		this.port = port;
		skrull_number = 0;
		day = 0;
		
		sSocket = new ServerSocket(port);
	}
	
	// client�� �޾� hashmap<String, Socket>�� �߰�
	public void waitForClient( ExecutorService eService) {
		
			try {
				client_sock = sSocket.accept();
				ChatThread ct = new ChatThread(client_sock);
				threads.add(ct);
				eService.execute(ct);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
	}
	
	public synchronized void addClient(String nickname, Socket clnt_sock) {
		client_socks.put(nickname, clnt_sock);
		client_socks_v.add(new Pair<Socket, Boolean>(clnt_sock, true));
		sendMsg(clnt_sock, nickname + "���� �����ϼ̽��ϴ�.");
	}
	
	public void removeClient(String nickname) {
		client_socks.remove(nickname);
		broadcast(nickname + "���� �����ϼ̽��ϴ�.");
		
	}
	
	public synchronized void addBuffer(Socket clnt_sock, BufferedReader br, PrintWriter out) {
		client_inBuffers.put(clnt_sock, new Pair<BufferedReader, Boolean>(br, true));
		client_outBuffers.put(clnt_sock, out);
	}
	
	
	public void night() {
		final String[] day_in_korean = {"ù", "��", "��", "��", "�ټ�", "����", "�ϰ�", "����", "��ȩ", "��"};
		
		broadcast(day_in_korean[day] + "��° ���� �Խ��ϴ�");
		
		if(day == 0)
			setSkrull();
		else 
			setKilled();
			
	}
	
	public void setSkrull() {
		broadcast("\nSkrull�� �����ϰڽ��ϴ�.");
		Random rand = new Random();
		skrull_number = rand.nextInt(max_client_num);
		for(int i = 0; i < max_client_num; i++) {
			if(i == skrull_number) 
				client_outBuffers.get(client_socks_v.get(i).getFirst()).println("----------\n| Skrull |\n----------");
			else
				client_outBuffers.get(client_socks_v.get(i).getFirst()).println("----------\n|  Human |\n----------");
		}
		
		broadcast("Skrull ������ �Ϸ�Ǿ����ϴ�.\n������ ������ �ٽ� �ѹ� Ȯ�����ּ���.");
	
	}
	
	public int setKilled() {
		broadcast("Skrull�� ���� �������� ���� �ֽ��ϴ�...");
		
		
		
		
		
		return 0;
	}
	
	public void morning() {
		broadcast("\n��ħ�� �Ǿ����ϴ�.");
		
		if(day++ != 0)
			showVoteResult();

		broadcast("2�е��� ����� ���� Skrull�� �ǽɵǴ� ����� �����ϼ���!");
		
//		try {
//			Thread.sleep(1000 * 60 * 2); //2�е��� ������ ����
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		
		getVote();

	}
	

	
	public int getVote() {
		
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		// ------------------------
		
		// Make All ReadThread pause
		VOTE_MODE = true;
		for(int i = 0; i < max_client_num;i++) {
			sendMsgToOne(client_socks_v.elementAt(i).getFirst(), "Command: ECHO");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// ------------------------
		
		//setAllInBufferOff(); // All Read Buffer Off. If all ReadThread is paused, this function might not be needed... need to think about this part again.
							
		new Thread(new WakeChatThread());

		
		broadcast("��ǥ�� �����ϰڽ��ϴ�.");
		
		client_socks
		.forEach((nickname, clnt)->{
			for( Pair<Socket, Boolean> wholive : client_socks_v) {
				if(wholive.getFirst() == clnt) {
					broadcast(nickname + "���� ��ǥ���Դϴ�.");
					
					
					
					break;
				}
			}
			
		});
		
		
		VOTE_MODE = false;
		
		
		
		return 0;
	}
	
	public int showVoteResult() {
		
		return 0;
	}
	
	public static synchronized void sendMsgToOne(Socket sock, String msg) {
		Socket clnt = sock;
		client_outBuffers.get(clnt).write(msg);
	}
	
	public static synchronized void sendMsg(Socket sock, String msg) {
		Socket clnt_from = sock;
		client_outBuffers.forEach((clnt, out)->{ if(clnt != clnt_from) out.println(msg);});
	}

	public static synchronized void broadcast(String msg) {
		client_outBuffers.forEach((clnt, out)->{out.println(msg);});
	}

	
	public static synchronized void setInBufferOn() {
		client_socks_v.forEach( 
			(clnt)->{ if(clnt.getSecond()) 
						client_inBuffers.get(clnt.getFirst()).setSecond(true);
					}
			);
	}
	
	public static synchronized void setAllInBufferOff() {
		client_inBuffers.forEach((clnt, in)->{in.setSecond(false);});
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
		for(int i = 0 ; i < max_client_num; ++i) {
			waitForClient(client_thread_pool); 
			broadcast((max_client_num - 1 - i ) + "�� ���ҽ��ϴ�.");
		}
		
		while(true) {
			
			night();
			
			morning();
			
			// Game ���� Ȯ��
			
		}
	}
	

	
	//-------------------------------------------
	class WakeChatThread extends ChatThread{
		public WakeChatThread() {
			
		}
		public void run() {
			this.notifyAll();
		}
	}
	
	class ChatThread extends Thread{
		private Socket clnt_sock;
		private BufferedReader br;
		private PrintWriter out;
		private String nickname;
		
		public ChatThread() {
			
		}
		
		public ChatThread(Socket client_sock) throws IOException {
			clnt_sock = client_sock;
			br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
			out = new PrintWriter(client_sock.getOutputStream(), true);
			nickname = br.readLine();
			
			addClient(nickname, client_sock);
			addBuffer(client_sock, br, out);
			
			sendMsg(clnt_sock, nickname + " has entered the chat");
		}
		

		
		@Override
		// ������� ���⼭ ó��
		// ������ ��� �����ֱ⸸ �ϸ� �ȴ�. 
		public void run() {
			// TODO Auto-generated method stub
			String client_msg;
			try {
				
				while( (client_msg = br.readLine()) != null){
					if(VOTE_MODE) {
						synchronized(this) {
							System.out.println(nickname + " is going to sleep");
							this.wait();
							System.out.println(nickname + " is awake");
							VOTE_MODE = false;
						}
					}
					else{
						if(client_inBuffers.get(clnt_sock).getSecond())
							sendMsg(clnt_sock, nickname + " : " + client_msg);
						else
							continue;
					}	
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				removeClient(nickname);
			}
		}
	}
}

class Pair<F, S>{
	F first;
	S second;
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	public void setfirst(F first) {
		this.first = first;
	}
	public F getFirst() {
		return this.first;
	}
	
	public void setSecond(S second) {
		this.second = second;
	}
	public S getSecond() {
		return this.second;
	}
}





