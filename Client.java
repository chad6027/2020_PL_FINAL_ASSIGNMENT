package aroundearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import aroundearth.Server;


//---------------------------------------------
//  
//
//
//---------------------------------------------
public class Client {
	private static String nickname;
	private static Socket client_sock;
	private static PrintWriter out;
	private static BufferedReader br;
	private static Scanner scv = new Scanner(System.in);
	
	public static void enterGame(int port) throws UnknownHostException, IOException {
		
		client_sock = new Socket("127.0.0.1", port);
		out = new PrintWriter(client_sock.getOutputStream(), true);
		br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
						
		out.println(nickname);
		
		ReadThread rT = new ReadThread();
		rT.run();
	}
	
	public static void discuss() {
		scv = new Scanner(System.in);
		while(true) {
			out.println(scv.nextLine());
		}
		//scv.close();
	}
	

	public static void vote() {
		
	}
	
	public static void setNickname() {
		System.out.println("사용할 닉네임을 입력해주세요.");
		nickname = scv.nextLine();
	}
	
	
	public static void main(String[] args) {
		setNickname();

		while(true) {
			
			System.out.println("1. 방 참가\n2. 방만들기");
			int particpate = scv.nextInt();
			int port;
			
			if(particpate == 1) {
				// port 번호 입력 후 enterGame()
				System.out.print("Port : ");
				port = scv.nextInt();
				
				try {
					enterGame(port);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			}
			else if(particpate == 2) {
				// 랜덤 생성한 port 번호를 이용하여 자동 참가
				Random rand = new Random();
				port = 8000 + rand.nextInt(2000);
				System.out.println("New Room's Port number : " + port);
				// Server를 쓰레드로 생성
				Server new_server = new Server(port);
				new_server.run();
				
				
				break;
			}
			else 
				System.out.println("옳지 않은 값을 입력하셨습니다");
		}
		
		// 게임이 종료될 때까지 반복
		while(true) {
		
			// 제한된 시간동안 입력
			
			discuss();
			
			vote();
			
			
		}
		
		//scv.close();
	}

	static class ReadThread implements Runnable {
		protected static BufferedReader br;
		
		public ReadThread() {
			this.br = Client.br;
		}
		
		@Override
		// 출력 스트림 분리
		public void run() {
			// TODO Auto-generated method stub
			String read_msg;
			
			try {
				while( (read_msg = br.readLine()) != null)
				{
					System.out.println(read_msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
}


