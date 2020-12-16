package aroundearth;

import java.util.Random;
import java.util.Scanner;
// 내일 마지막 전공 시험을 마치고 24일까지 완성하겠습니다.
public class Client implements Runnable{

	public static void enterGame(int port) {
		
	}
	
	public static void vote() {
		
	}
	
	
	public static void main(String[] args) {
		Scanner scv = new Scanner(System.in);
			
		while(true) {
			System.out.printf("1. 방 참가\n2. 방만들기");
			int particpate = scv.nextInt();
			int port;
			
			if(particpate == 1) {
				// port 번호 입력 후 enterGame()
				System.out.print("Port : ");
				port = scv.nextInt();
				
				enterGame(port);
				
				break;
			}
			else if(particpate == 2) {
				// 랜덤 생성한 port 번호를 이용하여 자동 참가
				Random rand = new Random();
				port = 8000 + rand.nextInt(2000);
				System.out.print("New Room's Port number : " + port);
				Server new_server = new Server(port);
				// Server를 쓰레드로 생성
				
				break;
			}
			else 
				System.out.printf("옳지 않은 값을 입력하셨습니다");
		}
		
		// 게임이 종료될 때까지 반복
		while(true) {
		
			// 제한된 시간동안 입력
			
			vote();
			
			
		}
		
	}
	

	



	@Override
	// 출력 스트림 분리
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
