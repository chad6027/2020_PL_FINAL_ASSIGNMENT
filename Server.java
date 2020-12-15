package aroundearth;

public class Server implements Runnable{
	
	public int port;
	
	public Server(int port) {
		this.port = port;
	}
	// client를 받아 hashmap<String, Socket>에 추가
	public void waitForClient() {
		
	}
	
	public int getVoting() {
		
		return 0;
	}
	
	public void setSkrull() {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int i = 0 ; i < 5; ++i) {
			waitForClient(); 
		}
		
		setSkrull();
		
		
		while(true) {

			// Client에게 채팅 시간 부여
			
			getVoting();
			
			// Game 종료 확인
			
		}
	}
	
	public void sendMessage() {
		
	}
	
	//-------------------------------------------
	class ChatThread implements Runnable{

		@Override
		// 입출력을 여기서 처리
		// 서버는 듣고 보내주기만 하면 된다.
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}

