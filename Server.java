package aroundearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class Server implements Runnable{
	
	public int port;
	public String who = new String("");
	
	private int day;
	private int num_of_lives;
	private int num_of_clients;
	private Socket sock_skrull;

	private final int max_clients_num = 4;
	private final int SKRULL = 0;
	private final int HUMAN = 1;
	
	private boolean VOTE_MODE = false;
	private boolean KILL_MODE = false;
	private ExecutorService client_thread_pool = Executors.newFixedThreadPool(max_clients_num);
	
	private  static Socket client_sock;
	private  static ServerSocket sSocket;
	
	private static Vector<Pair<Socket, Boolean>> client_socks_v= new Vector<Pair<Socket, Boolean>>(); 
	private  static HashMap<String, Socket> client_socks = new HashMap<String, Socket>();
	private  static HashMap<Socket, Pair<BufferedReader, Boolean>> client_inBuffers = new HashMap<Socket, Pair<BufferedReader, Boolean>>();
	private  static HashMap<Socket, PrintWriter> client_outBuffers = new HashMap<Socket, PrintWriter>();
	
	public Server(int port) throws IOException {
		this.port = port;
		day = 0;
		num_of_clients = 0;
		sSocket = new ServerSocket(port);
	}
	
	// client�� �޾� hashmap<String, Socket>�� �߰�
	public void waitForClient( ExecutorService eService) {
		
			try {
				client_sock = sSocket.accept();
				ChatThread ct = new ChatThread(client_sock);
				eService.execute(ct);
				num_of_clients++;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	// Client �߰�
	public synchronized void addClient(String nickname, Socket clnt_sock) {
		client_socks.put(nickname, clnt_sock);
		client_socks_v.add(new Pair<Socket, Boolean>(clnt_sock, true));
		broadcast(nickname + "���� �����ϼ̽��ϴ�.");
	}
	// Client ����
	public void removeClient(String nickname) {
		Socket clnt = client_socks.get(nickname);

		for(int i = 0 ; i < client_socks_v.size(); i++) {
			if(client_socks_v.elementAt(i).getFirst() == clnt)
			{
				client_socks_v.remove(i);
				break;
			}
		}
		
		client_inBuffers.remove(clnt);
		client_outBuffers.remove(clnt);
		client_socks.remove(nickname);
		
		num_of_clients--;
		if(clnt != sock_skrull)
			num_of_lives--;
		
		broadcast(nickname + "���� �����ϼ̽��ϴ�.");
	}
	
	public synchronized void addBuffer(Socket clnt_sock, BufferedReader br, PrintWriter out) {
		client_inBuffers.put(clnt_sock, new Pair<BufferedReader, Boolean>(br, true));
		client_outBuffers.put(clnt_sock, out);
	}
	
	
	public void night() throws GameEndException {
		final String[] day_in_korean = {"ù", "��", "��", "��", "�ټ�", "����", "�ϰ�", "����", "��ȩ", "��"};
		
		broadcast(day_in_korean[day] + "��° ���� �Ǿ����ϴ�");
		
		if(day == 0)
			setSkrull();
		else 
			setKilled();
			
		checkGameEnd();
	}
	
	

	public void setSkrull() {
		broadcast("\nSkrull�� �����ϰڽ��ϴ�.");
		Random rand = new Random();
		sock_skrull = client_socks_v.elementAt(rand.nextInt(max_clients_num)).getFirst();
		
		for(int i = 0; i < max_clients_num; i++) {
			if(client_socks_v.elementAt(i).getFirst() == sock_skrull) 
				client_outBuffers.get(client_socks_v.get(i).getFirst()).println("----------\n| Skrull |\n----------");
			else
				client_outBuffers.get(client_socks_v.get(i).getFirst()).println("----------\n|  Human |\n----------");
		}
		
		broadcast("Skrull ������ �Ϸ�Ǿ����ϴ�.\n������ ������ �ٽ� �ѹ� Ȯ�����ּ���.");
	
	}
	
	public int setKilled() {
		
		Socket sockIsKilled;
		// (key : nickname, value : client sock) ---> ( key : client sock, value : nickname)
		HashMap<Socket, String> client_socks_inverse = 
				(HashMap<Socket, String>) client_socks.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		
		setAllInBufferOff();
		
		
		broadcast("\nSkrull�� ���� �������� ���� �ֽ��ϴ�...");
		sendMsgToOne(sock_skrull, "\n���� �޼����� Skrull���Ը� ���Դϴ�.\n���� ����ִ� ��������\n"
				+ "------------------------------");
		
		client_socks_v.stream()
		.filter( P -> P.getFirst() != sock_skrull && P.getSecond() )
		.forEach( P -> sendMsgToOne(sock_skrull, client_socks_inverse.get(P.getFirst())));
				
		sendMsgToOne(sock_skrull, "------------------------------\n" 
				+ "�Դϴ�.");
		
		KILL_MODE = true;
		client_inBuffers.get(sock_skrull).setSecond(true);
		
		sendMsgToOne(sock_skrull, "\n���� �������� �������ּ���.\n");
	
		MBox(sock_skrull);
		
		sockIsKilled = client_socks.get(who);
		client_inBuffers.get(sock_skrull).setSecond(false);
		KILL_MODE = false;
	
		// ���� client�� ���� update
		for(Pair<Socket, Boolean> sock : client_socks_v) {
			if(sock.getFirst() == sockIsKilled) {
				sock.setSecond(false);
				break;
			}
		}
		num_of_lives--;
		
		
		setInBufferOn();
		return 0;
	}
	
	public void morning() throws GameEndException {
		broadcast("\n��ħ�� �Ǿ����ϴ�.");
		
		if(day++ != 0)
			showKilledResult();

		broadcast("2�е��� ����� ���� Skrull�� �ǽɵǴ� ����� �����ϼ���!\n");
		
//		try {
//			Thread.sleep(1000 * 60 * 2); //2�е��� ������ ����
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// test�� 10�ʸ� ����
		try {
			Thread.sleep(1000 * 10); //10��
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		getVote();

		checkGameEnd();
	}
	

	
	public int getVote() {
		
	
	
		// (key : nickname, value : client sock) ---> ( key : client sock, value : nickname)
		HashMap<Socket, String> client_socks_inverse = 
				(HashMap<Socket, String>) client_socks.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		// ��ǥ ���� ��Ƴ��� HashMap result
		HashMap<String, Integer> result = new HashMap<String, Integer>();	
		client_socks_v.forEach( P -> { if(P.getSecond()) result.put(client_socks_inverse.get(P.getFirst()), 0);});
		
		
		// ------------------------
		int max_vote = 0;
		String whoIsKilled = new String();
		Socket sockIsKilled;
		
		VOTE_MODE = true;
		setAllInBufferOff();

		// ������ ��� ����
		broadcast("\n��ǥ�� �����ϰڽ��ϴ�.\n���� ������ �����\n");

		for(Pair<Socket, Boolean> wholive : client_socks_v) {
			if(wholive.getSecond()) {
				broadcast(client_socks_inverse.get(wholive.getFirst()));
			}
		}
		
		broadcast("\n�Դϴ�.");
		
		// �Ѹ� ��ǥ
		for(Pair<Socket, Boolean> wholive : client_socks_v) {
			if(wholive.getSecond()) {
				String nickname = client_socks_inverse.get(wholive.getFirst());
				Socket clnt = wholive.getFirst();
				
				broadcast(nickname + "���� ��ǥ���Դϴ�.\n");
				client_inBuffers.get(clnt).setSecond(true);
				
				MBox(clnt);
				
				client_inBuffers.get(clnt).setSecond(false);
				result.put(who, result.get(who) + 1);
			}
		}
		
		
		broadcast("\n��ǥ�� ����Ǿ����ϴ�.");
		
		
		// ��ǥ ��� Ȯ��
		for( String nick : result.keySet()) {
			if(result.get(nick) > max_vote) {
				whoIsKilled = nick;
				max_vote = result.get(nick); 
			}
		}
		
		// �ִ� ��ǥ�� �θ����� Ȯ�� --> �ƹ��� ���� �ʴ´�.
		for( String nick : result.keySet()) {
			if(nick != whoIsKilled && result.get(nick) == max_vote) {
				whoIsKilled = ""; 
			}
		}
		
		
		// ��� ����
		if(whoIsKilled.isEmpty())
			broadcast("\n��ǥ ��� �ƹ��� ���� �ʾҽ��ϴ�.");
		else {
			broadcast("\n��ǥ ��� " + whoIsKilled + "���� �׾����ϴ�.");
			
			sockIsKilled = client_socks.get(whoIsKilled); 
			
			if(sockIsKilled == sock_skrull)
				broadcast(whoIsKilled + "���� Skrull �̾����ϴ�.");
			else {
				broadcast(whoIsKilled + "���� Human �̾����ϴ�.");
				num_of_lives--;
			}
			
			// ���� client�� ���� update
			for(Pair<Socket, Boolean> sock : client_socks_v) {
				if(sock.getFirst() == sockIsKilled) {
					sock.setSecond(false);
					break;
				}
			}
		}
		
		setInBufferOn(); // ����ִ� client���� inBuffer On
		VOTE_MODE = false;		
		return 0;
	}
	
	public void showKilledResult() {
		broadcast("\n���㿡 Skrull�� ����" + who + "���� �׾����ϴ�.");
	}
	
	// ChatThread��� ����ȭ
	public void MBox(Socket sock) {
			synchronized (sock) {
				try {
					sock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	public void checkGameEnd() throws GameEndException {
		boolean isDone = false;
		int winner = HUMAN;

		if(num_of_lives == 1) {
			isDone = true; // skrull�� Human�� ���� �Ѹ� ������ skrull�� �¸�
			winner = SKRULL;
		}
		else {
			for(Pair<Socket, Boolean> sock : client_socks_v) {
				if(sock.getFirst() == sock_skrull) {
					if(!sock.getSecond()) { // skrull�� �׾�����
						isDone = true;
						winner = HUMAN;
					}
					break;
				}
			}
		}
		
		
		if(isDone) {
			String EndMsg = new String();
			//
			if(winner == HUMAN) 
				EndMsg = "\n\n\n*****************		Skrull�� �׾� Human�� �¸��Դϴ� !!		*****************|\n\n\n";
						
			
			else 
				EndMsg = "\n\n\n*****************		Human�� �Ѹ� ���� �̱� �� �����Ƿ� Skrull�� �¸��Դϴ� !!		*****************|\n\n\n"; 	
			
			throw new GameEndException(EndMsg);
			
		}
	}
	
	// �� Ŭ���̾�Ʈ���Ը� �޼��� 
	public static synchronized void sendMsgToOne(Socket sock, String msg) {
		Socket clnt = sock;
		client_outBuffers.get(clnt).println(msg);
	}
	// ��� Ŭ���̾�Ʈ���� �޼���
	public static synchronized void broadcast(String msg) {
		client_outBuffers.forEach((clnt, out)->{out.println(msg);});
	}

	// Client�� �� ����ִ� Ŭ���̾�Ʈ�� �޼����� �޴´�.
	public static void setInBufferOn() {
		client_socks_v
		.forEach( 
			(clnt)->{ if(clnt.getSecond()) 
						client_inBuffers.get(clnt.getFirst()).setSecond(true);
					}
			);
	}
	
	// ��� �޼��� ����
	public static void setAllInBufferOff() {
		client_inBuffers.forEach( (clnt, in)->{in.setSecond(false);});
	}
	
	
	public void startGame() {
		broadcast("\n\n\n******************// Game Start //******************\n\n\n");
		num_of_lives = max_clients_num - 1;
		day = 0;
		
		for(Pair<Socket, Boolean> clnt : client_socks_v) {
			clnt.setSecond(true);
		}
		
		setInBufferOn();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true) {
			broadcast("\nWaiting For Players...");
			
			for(int i = num_of_clients ; i < max_clients_num; ++i) {
				waitForClient(client_thread_pool); 
				broadcast((max_clients_num - 1 - i ) + "�� ���ҽ��ϴ�.");
			}
			
			//Game start -------------------------------
			startGame();
			
			while(true) {
				try {
					night();			
					morning();
				}
				catch(GameEndException e) {			// Game ���� exception �߻�
					broadcast(e.getMessage());
					broadcast("\n10�� �� ���� ������ �����մϴ�...");
					
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e1) {

					}
					
					break;
				}
			}
			//------------------------------------------
		}
		
	}
	
	//------------------------------------------
	
	class ChatThread extends Thread{
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
			addBuffer(client_sock, br, out);
			
			broadcast(nickname + " has entered the chat");
		}
		

		@Override
		// ������� ���⼭ ó��
		// ������ ��� �����ֱ⸸ �ϸ� �ȴ�. 
		public void run() {
			// TODO Auto-generated method stub
			String client_msg;
			try {
				
				while( (client_msg = br.readLine()) != null){
					// inBuffer�� false�� �޼��� ����
					if(!client_inBuffers.get(clnt_sock).getSecond()) {
						continue;
					}
					
					// MODE ON
					if(VOTE_MODE || KILL_MODE) {
						
						// ���� String�� �ڱ� �ڽ��� �ƴϰ�, �ٸ� �̿����� nickname�̸�
						if(!nickname.equals(client_msg) && client_socks.containsKey(client_msg)) {
							boolean isLive = false;
							for( Pair<Socket, Boolean> wholive : client_socks_v) 
								if(wholive.getFirst()== client_socks.get(client_msg)) {
									isLive = true; 
									break;
								}
							
							
							// ����ִ� ����̶��
							if(isLive)
								synchronized(clnt_sock) {
									who = client_msg;
									clnt_sock.notify(); 	// main thread�� lock�� Ǯ��
								}
							//���� ���
							else
								sendMsgToOne(clnt_sock, "�̹� ���� ����� ���� �� �����ϴ�.");
							
							//-------------------------------------------------------------------------------------------------
							//  
//							Map<Socket, Boolean> survivors_map = client_socks_v.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
//							
							// ����ִٸ�
//							if(survivors_map.get(client_socks.get(client_msg)))
//								synchronized(clnt_sock) {
//									who = client_msg;
//									clnt_sock.notify(); 	// main thread�� lock�� Ǯ��
//								}
							// �׾��ٸ�
//							else
//								sendMsgToOne(clnt_sock, "�̹� ���� ����� ���� �� �����ϴ�.");
							//--------------------------------------------------------------------------------------------------
							
							
						}
						else {
							if(nickname.equals(client_msg))
								sendMsgToOne(clnt_sock, "�ڱ� �ڽ��� ���� �� �����ϴ�.");
							else
								sendMsgToOne(clnt_sock, "���� ����� �̸��� ��Ȯ�ϰ� �Է����ּ���");
						}
							
					}
					
					else
						broadcast(nickname + " : " + client_msg); // Chatting
					
				}
			} catch (IOException e) {
				// client�� ���� ����
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






