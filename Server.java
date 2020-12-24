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
	
	// client를 받아 hashmap<String, Socket>에 추가
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
	// Client 추가
	public synchronized void addClient(String nickname, Socket clnt_sock) {
		client_socks.put(nickname, clnt_sock);
		client_socks_v.add(new Pair<Socket, Boolean>(clnt_sock, true));
		broadcast(nickname + "님이 입장하셨습니다.");
	}
	// Client 제거
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
		
		broadcast(nickname + "님이 퇴장하셨습니다.");
	}
	
	public synchronized void addBuffer(Socket clnt_sock, BufferedReader br, PrintWriter out) {
		client_inBuffers.put(clnt_sock, new Pair<BufferedReader, Boolean>(br, true));
		client_outBuffers.put(clnt_sock, out);
	}
	
	
	public void night() throws GameEndException {
		final String[] day_in_korean = {"첫", "두", "세", "네", "다섯", "여섯", "일곱", "여덟", "아홉", "열"};
		
		broadcast(day_in_korean[day] + "번째 밤이 되었습니다");
		
		if(day == 0)
			setSkrull();
		else 
			setKilled();
			
		checkGameEnd();
	}
	
	

	public void setSkrull() {
		broadcast("\nSkrull을 선정하겠습니다.");
		Random rand = new Random();
		sock_skrull = client_socks_v.elementAt(rand.nextInt(max_clients_num)).getFirst();
		
		for(int i = 0; i < max_clients_num; i++) {
			if(client_socks_v.elementAt(i).getFirst() == sock_skrull) 
				client_outBuffers.get(client_socks_v.get(i).getFirst()).println("----------\n| Skrull |\n----------");
			else
				client_outBuffers.get(client_socks_v.get(i).getFirst()).println("----------\n|  Human |\n----------");
		}
		
		broadcast("Skrull 선정이 완료되었습니다.\n각자의 역할을 다시 한번 확인해주세요.");
	
	}
	
	public int setKilled() {
		
		Socket sockIsKilled;
		// (key : nickname, value : client sock) ---> ( key : client sock, value : nickname)
		HashMap<Socket, String> client_socks_inverse = 
				(HashMap<Socket, String>) client_socks.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		
		setAllInBufferOff();
		
		
		broadcast("\nSkrull이 죽일 지구인을 고르고 있습니다...");
		sendMsgToOne(sock_skrull, "\n지금 메세지는 Skrull에게만 보입니다.\n현재 살아있는 지구인은\n"
				+ "------------------------------");
		
		client_socks_v.stream()
		.filter( P -> P.getFirst() != sock_skrull && P.getSecond() )
		.forEach( P -> sendMsgToOne(sock_skrull, client_socks_inverse.get(P.getFirst())));
				
		sendMsgToOne(sock_skrull, "------------------------------\n" 
				+ "입니다.");
		
		KILL_MODE = true;
		client_inBuffers.get(sock_skrull).setSecond(true);
		
		sendMsgToOne(sock_skrull, "\n죽일 지구인을 선택해주세요.\n");
	
		MBox(sock_skrull);
		
		sockIsKilled = client_socks.get(who);
		client_inBuffers.get(sock_skrull).setSecond(false);
		KILL_MODE = false;
	
		// 죽은 client의 정보 update
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
		broadcast("\n아침이 되었습니다.");
		
		if(day++ != 0)
			showKilledResult();

		broadcast("2분동안 토론을 통해 Skrull로 의심되는 사람을 결정하세요!\n");
		
//		try {
//			Thread.sleep(1000 * 60 * 2); //2분동안 쓰레드 정지
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// test로 10초만 정지
		try {
			Thread.sleep(1000 * 10); //10초
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
		
		// 투표 수를 모아놓는 HashMap result
		HashMap<String, Integer> result = new HashMap<String, Integer>();	
		client_socks_v.forEach( P -> { if(P.getSecond()) result.put(client_socks_inverse.get(P.getFirst()), 0);});
		
		
		// ------------------------
		int max_vote = 0;
		String whoIsKilled = new String();
		Socket sockIsKilled;
		
		VOTE_MODE = true;
		setAllInBufferOff();

		// 생존자 목록 전송
		broadcast("\n투표를 시작하겠습니다.\n현재 생존자 목록은\n");

		for(Pair<Socket, Boolean> wholive : client_socks_v) {
			if(wholive.getSecond()) {
				broadcast(client_socks_inverse.get(wholive.getFirst()));
			}
		}
		
		broadcast("\n입니다.");
		
		// 한명씩 투표
		for(Pair<Socket, Boolean> wholive : client_socks_v) {
			if(wholive.getSecond()) {
				String nickname = client_socks_inverse.get(wholive.getFirst());
				Socket clnt = wholive.getFirst();
				
				broadcast(nickname + "님이 투표중입니다.\n");
				client_inBuffers.get(clnt).setSecond(true);
				
				MBox(clnt);
				
				client_inBuffers.get(clnt).setSecond(false);
				result.put(who, result.get(who) + 1);
			}
		}
		
		
		broadcast("\n투표가 종료되었습니다.");
		
		
		// 투표 결과 확인
		for( String nick : result.keySet()) {
			if(result.get(nick) > max_vote) {
				whoIsKilled = nick;
				max_vote = result.get(nick); 
			}
		}
		
		// 최다 득표가 두명인지 확인 --> 아무도 죽지 않는다.
		for( String nick : result.keySet()) {
			if(nick != whoIsKilled && result.get(nick) == max_vote) {
				whoIsKilled = ""; 
			}
		}
		
		
		// 결과 공지
		if(whoIsKilled.isEmpty())
			broadcast("\n투표 결과 아무도 죽지 않았습니다.");
		else {
			broadcast("\n투표 결과 " + whoIsKilled + "님이 죽었습니다.");
			
			sockIsKilled = client_socks.get(whoIsKilled); 
			
			if(sockIsKilled == sock_skrull)
				broadcast(whoIsKilled + "님은 Skrull 이었습니다.");
			else {
				broadcast(whoIsKilled + "님은 Human 이었습니다.");
				num_of_lives--;
			}
			
			// 죽은 client의 정보 update
			for(Pair<Socket, Boolean> sock : client_socks_v) {
				if(sock.getFirst() == sockIsKilled) {
					sock.setSecond(false);
					break;
				}
			}
		}
		
		setInBufferOn(); // 살아있는 client들의 inBuffer On
		VOTE_MODE = false;		
		return 0;
	}
	
	public void showKilledResult() {
		broadcast("\n간밤에 Skrull에 의해" + who + "님이 죽었습니다.");
	}
	
	// ChatThread들과 동기화
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
			isDone = true; // skrull과 Human이 각각 한명씩 남으면 skrull의 승리
			winner = SKRULL;
		}
		else {
			for(Pair<Socket, Boolean> sock : client_socks_v) {
				if(sock.getFirst() == sock_skrull) {
					if(!sock.getSecond()) { // skrull이 죽었으면
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
				EndMsg = "\n\n\n*****************		Skrull이 죽어 Human의 승리입니다 !!		*****************|\n\n\n";
						
			
			else 
				EndMsg = "\n\n\n*****************		Human이 한명 남아 이길 수 없으므로 Skrull의 승리입니다 !!		*****************|\n\n\n"; 	
			
			throw new GameEndException(EndMsg);
			
		}
	}
	
	// 한 클라이언트에게만 메세지 
	public static synchronized void sendMsgToOne(Socket sock, String msg) {
		Socket clnt = sock;
		client_outBuffers.get(clnt).println(msg);
	}
	// 모든 클라이언트에게 메세지
	public static synchronized void broadcast(String msg) {
		client_outBuffers.forEach((clnt, out)->{out.println(msg);});
	}

	// Client들 중 살아있는 클라이언트의 메세지만 받는다.
	public static void setInBufferOn() {
		client_socks_v
		.forEach( 
			(clnt)->{ if(clnt.getSecond()) 
						client_inBuffers.get(clnt.getFirst()).setSecond(true);
					}
			);
	}
	
	// 모든 메세지 무시
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
				broadcast((max_clients_num - 1 - i ) + "명 남았습니다.");
			}
			
			//Game start -------------------------------
			startGame();
			
			while(true) {
				try {
					night();			
					morning();
				}
				catch(GameEndException e) {			// Game 종료 exception 발생
					broadcast(e.getMessage());
					broadcast("\n10초 후 다음 게임을 시작합니다...");
					
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
		// 입출력을 여기서 처리
		// 서버는 듣고 보내주기만 하면 된다. 
		public void run() {
			// TODO Auto-generated method stub
			String client_msg;
			try {
				
				while( (client_msg = br.readLine()) != null){
					// inBuffer가 false면 메세지 무시
					if(!client_inBuffers.get(clnt_sock).getSecond()) {
						continue;
					}
					
					// MODE ON
					if(VOTE_MODE || KILL_MODE) {
						
						// 받은 String이 자기 자신이 아니고, 다른 이용자의 nickname이면
						if(!nickname.equals(client_msg) && client_socks.containsKey(client_msg)) {
							boolean isLive = false;
							for( Pair<Socket, Boolean> wholive : client_socks_v) 
								if(wholive.getFirst()== client_socks.get(client_msg)) {
									isLive = true; 
									break;
								}
							
							
							// 살아있는 사람이라면
							if(isLive)
								synchronized(clnt_sock) {
									who = client_msg;
									clnt_sock.notify(); 	// main thread의 lock이 풀림
								}
							//죽은 사람
							else
								sendMsgToOne(clnt_sock, "이미 죽은 사람은 뽑을 수 없습니다.");
							
							//-------------------------------------------------------------------------------------------------
							//  
//							Map<Socket, Boolean> survivors_map = client_socks_v.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
//							
							// 살아있다면
//							if(survivors_map.get(client_socks.get(client_msg)))
//								synchronized(clnt_sock) {
//									who = client_msg;
//									clnt_sock.notify(); 	// main thread의 lock이 풀림
//								}
							// 죽었다면
//							else
//								sendMsgToOne(clnt_sock, "이미 죽은 사람은 뽑을 수 없습니다.");
							//--------------------------------------------------------------------------------------------------
							
							
						}
						else {
							if(nickname.equals(client_msg))
								sendMsgToOne(clnt_sock, "자기 자신을 뽑을 수 없습니다.");
							else
								sendMsgToOne(clnt_sock, "뽑을 사람의 이름을 정확하게 입력해주세요");
						}
							
					}
					
					else
						broadcast(nickname + " : " + client_msg); // Chatting
					
				}
			} catch (IOException e) {
				// client의 접속 종료
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






