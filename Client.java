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
		
		rT.run(); // ���⼭ ����Ǵ� issue why?
	}
	
	public static void discuss() {
		String msg;
		while(true) {
			msg = scv.nextLine();
			out.println(msg);
		}
	}
	

	public static void vote() {
		
	}
	
	public static void setNickname() {
		System.out.println("����� �г����� �Է����ּ���.");
		nickname = scv.nextLine();
	}
	
	
	public static void main(String[] args) {
		setNickname();

		while(true) {
			
			System.out.println("1. �� ����\n2. �� �����");
			int particpate = scv.nextInt();
			int port;
			
			if(particpate == 1) {
				// port ��ȣ �Է� �� enterGame()
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
				// ���� ������ port ��ȣ�� �̿��Ͽ� �ڵ� ����
				Random rand = new Random();
				port = 8000 + rand.nextInt(2000);
				System.out.println("New Room's Port number : " + port);
				// Server�� ������� ����
				Server new_server = new Server(port);
				new_server.run();
				
				
				break;
			}
			else 
				System.out.println("���� ���� ���� �Է��ϼ̽��ϴ�");
		}
		
		// ������ ����� ������ �ݺ�
		while(true) {
		
			// ���ѵ� �ð����� �Է�
			
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
		// ��� ��Ʈ�� �и�
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


