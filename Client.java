package aroundearth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import aroundearth.Server;

public class Client {
	private static String nickname;
	private static Socket client_sock;
	private static PrintWriter out;
	private static BufferedReader br;
	private static Scanner scv = new Scanner(System.in);
	private static Thread rT;
	private static Thread wT;
	
	
	
	public static void enterGame(int port) throws IOException {
		
		client_sock = new Socket("127.0.0.1", port);
		out = new PrintWriter(client_sock.getOutputStream(), true);
		br = new BufferedReader(new InputStreamReader(client_sock.getInputStream()));
						
		out.println(nickname);
		
		ReadThread r = new ReadThread();
		WriteThread w = new WriteThread();
		
		rT = new Thread(r);
		wT = new Thread(w);
		
		rT.start();
		wT.start();
	}

	public static void setNickname() {
		System.out.println("����� �г����� �Է����ּ���.");
		nickname = scv.nextLine();
	}
	
	
	public static void main(String[] args) {
		
		setNickname();
		int particpate= -1;
		int port = -1;

		while(true) {
			System.out.println("1. �� ����\n2. �� �����");
			try {
				particpate = scv.nextInt();
				if(particpate != 1 && particpate != 2 ) {
					System.out.println("�߸��� ��ȣ�� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���.\n");
					continue;
				}
				else
					break;
			}
			catch(InputMismatchException e) {
				// ���� �߻����� scv �ٽ� ����
				scv = new Scanner(System.in);
				System.out.println("1 �Ǵ� 2�� �Է����ּ���.\n");
			}
		}
		

		while(true) {
					
			if(particpate == 1) {
				// port ��ȣ �Է� �� enterGame()
				System.out.print("Port : ");
				try {
					port = scv.nextInt();
					if(port < 8000 || port > 9999) {
						System.out.println("Port ��ȣ�� 8000 ~ 9999 ������ �����Դϴ�. �ٽ� �Է����ּ���.\n");				
						continue;
					}
				}
				catch(InputMismatchException e) {
					// ���� �߻����� scv �ٽ� ����
					scv = new Scanner(System.in);
					System.out.println("Port ��ȣ�� 8000 ~ 9999 ������ �����Դϴ�. �ٽ� �Է����ּ���.\n");
					continue;
				}
				
			}
			else {
				// ���� ������ port ��ȣ�� �̿��Ͽ� �ڵ� ����
				Random rand = new Random();
				port = 8000 + rand.nextInt(2000);
				System.out.println("New Room's Port number : " + port);
				
				//create Server
				try {
					Server new_server = new Server(port);
					new Thread(new_server).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					
					System.out.println("Fail to create Server");
				}
			}

			try {
				enterGame(port);
			} catch (IOException e) {
			
				System.out.println("���� ���ӿ� �����߽��ϴ�. ");
			} 
			
			
			break;
		}
		
		
		
		
		// ������ ����� ������ �ݺ�
		while(true) {
			
			synchronized(rT) {
				try {
					rT.wait(); // Server�� ����� ���� ������ wait
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			wT.interrupt();
			scv.close();
			break;
		}
	}

	static class ReadThread implements Runnable {
		protected BufferedReader br;
		
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
				
				// Server�� FIN�� ������ ���� ����
				synchronized(this) {
					this.notify();
				}
				
			}
		}
	}
	
	static class WriteThread implements Runnable {
		protected PrintWriter out;
		
		public WriteThread() {
			this.out = Client.out;
		}
		
		@Override
		// ��� ��Ʈ�� �и�
		public void run() {
			// TODO Auto-generated method stub
			String msg;
			while(true) {
				try {
					msg = scv.nextLine();
					out.println(msg);
					out.flush();
					Thread.sleep(1);
				} catch (InterruptedException e) {
						System.out.println("Server has been closed\n");
						break;
				}
			}
		}
	}
}



