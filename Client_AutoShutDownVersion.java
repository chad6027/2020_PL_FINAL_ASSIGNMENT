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
	private static BufferedReader test_br = new BufferedReader(new InputStreamReader(System.in));
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
		try {
			nickname = test_br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		
		setNickname();
		int particpate= -1;
		int port = -1;

		while(true) {
			System.out.println("1. �� ����\n2. �� �����");
			try {
				particpate = Integer.parseInt((test_br.readLine()));
			}
			catch(InputMismatchException | NumberFormatException | IOException e) {
				System.out.println("�߸� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���.\n");
			}
			
			if(particpate != 1 && particpate != 2)
				System.out.println("�߸� �Է��ϼ̽��ϴ�. �ٽ� �Է����ּ���.\\n");
			else
				break;
		}
		

		while(true) {
					
			if(particpate == 1 || particpate == 2) {
				if(particpate == 1) {
					// port ��ȣ �Է� �� enterGame()
					System.out.print("Port : ");
					try {
						port = Integer.parseInt(test_br.readLine());
					}
					catch(InputMismatchException | NumberFormatException | IOException e) {
						System.out.println("Port ��ȣ�� 8000 ~ 9999 ������ �����Դϴ�.\n�ٽ� �Է����ּ���.");
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
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("���� ���ӿ� �����߽��ϴ�. ");
				} 
				
				
				break;
			}
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
		protected BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
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
					//interrupt�� ���� ���� 100ms���� �Է��� ������ Thread sleep
					while(!test_br.ready())
						Thread.sleep(2000);
					
					msg = test_br.readLine();
					out.println(msg);
					out.flush();
				
				} catch (InterruptedException | IOException e) {
						System.out.println("Server has been closed\n");
						break;
				}
			}
		}
	}
	
}



