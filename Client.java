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
	private static boolean writeThread_on = true;
	
	public static void enterGame(int port) throws UnknownHostException, IOException {
		
		client_sock = new Socket("127.0.0.1", port);
		out = new PrintWriter(client_sock.getOutputStream(), true);
		br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
						
		out.println(nickname);
		
		ReadThread rT = new ReadThread();
		new Thread(rT).start();
	}
	
	public static void discuss() {
		WriteThread wT = new WriteThread();
		new Thread(wT).start();
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
			
			System.out.println("1. 방 참가\n2. 방 만들기");
			int particpate = scv.nextInt();
			int port;
			
			if(particpate == 1 || particpate == 2) {
				if(particpate == 1) {
					// port 번호 입력 후 enterGame()
					System.out.print("Port : ");
					port = scv.nextInt();
				}
				else {
					// 랜덤 생성한 port 번호를 이용하여 자동 참가
					Random rand = new Random();
					port = 8000 + rand.nextInt(2000);
					System.out.println("New Room's Port number : " + port);
					
					
					//create Server
					try {
						Server new_server = new Server(port);
						new Thread(new_server).start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
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
			else 
				System.out.println("옳지 않은 값을 입력하셨습니다");
		}
		
		// 게임이 종료될 때까지 반복
		while(true) {
		
			// 제한된 시간동안 입력
			
			discuss();
			
			try {
				Thread.sleep(1000 * 60 * 5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
					//Server에서 command를 보내는 경우
					if(read_msg.equals("Command: ECHO") || read_msg.equals("Command: KILL")) {
						out.println(read_msg);
					}
					//command가 아니면 터미널에 출력
					else
						System.out.println(read_msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static class WriteThread implements Runnable {
		protected static PrintWriter out;
		
		public WriteThread() {
			this.out = Client.out;
		}
		
		@Override
		// 출력 스트림 분리
		public void run() {
			// TODO Auto-generated method stub
			String msg;
			while(true) {
				msg = scv.nextLine();
				out.println(msg);
				out.flush();
			}
		}
	}
}


