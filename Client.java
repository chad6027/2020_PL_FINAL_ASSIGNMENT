package aroundearth;

import java.util.Random;
import java.util.Scanner;
// ���� ������ ���� ������ ��ġ�� 24�ϱ��� �ϼ��ϰڽ��ϴ�.
public class Client implements Runnable{

	public static void enterGame(int port) {
		
	}
	
	public static void vote() {
		
	}
	
	
	public static void main(String[] args) {
		Scanner scv = new Scanner(System.in);
			
		while(true) {
			System.out.printf("1. �� ����\n2. �游���");
			int particpate = scv.nextInt();
			int port;
			
			if(particpate == 1) {
				// port ��ȣ �Է� �� enterGame()
				System.out.print("Port : ");
				port = scv.nextInt();
				
				enterGame(port);
				
				break;
			}
			else if(particpate == 2) {
				// ���� ������ port ��ȣ�� �̿��Ͽ� �ڵ� ����
				Random rand = new Random();
				port = 8000 + rand.nextInt(2000);
				System.out.print("New Room's Port number : " + port);
				Server new_server = new Server(port);
				// Server�� ������� ����
				
				break;
			}
			else 
				System.out.printf("���� ���� ���� �Է��ϼ̽��ϴ�");
		}
		
		// ������ ����� ������ �ݺ�
		while(true) {
		
			// ���ѵ� �ð����� �Է�
			
			vote();
			
			
		}
		
	}
	

	



	@Override
	// ��� ��Ʈ�� �и�
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
